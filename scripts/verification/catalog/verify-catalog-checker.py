"""Test catalog verification using altered JAR copies. Never modify the real mod or its sources."""
import argparse
import copy
from concurrent.futures import ThreadPoolExecutor
import json
from pathlib import Path
import re
import subprocess
import zipfile


ROOT = Path(__file__).resolve().parents[3]


def objects(node):
    if isinstance(node, dict):
        yield node
        for child in node.values():
            yield from objects(child)
    elif isinstance(node, list):
        for child in node:
            yield from objects(child)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--powershell', default='pwsh', help='PowerShell 7 executable')
    parser.add_argument('--jar', type=Path)
    parser.add_argument('--manifest', type=Path, default=ROOT / 'build/game-test-run/catalog-manifest.json')
    parser.add_argument('--workers', type=int, default=4, help='Parallel read-only checker processes; use 1 to run serially')
    args = parser.parse_args()
    if args.workers < 1:
        parser.error('--workers must be positive')
    properties = dict(re.findall(r'^(mod_id|mod_version)=(.+)$', (ROOT / 'gradle.properties').read_text(), re.M))
    jar = args.jar or ROOT / f'build/libs/{properties["mod_id"]}-{properties["mod_version"]}.jar'
    with zipfile.ZipFile(jar) as archive:
        original = {name: archive.read(name) for name in archive.namelist() if not name.endswith('/')}
    manifest = json.loads(args.manifest.read_text(encoding='utf-8-sig'))
    if manifest.get('schemaVersion') != 8:
        parser.error('Run scripts/verify-mod.ps1 first to export the current catalog manifest.')
    output = ROOT / 'build/catalog-checker-probes'
    output.mkdir(parents=True, exist_ok=True)
    cases = [('unchanged', {}, None)]

    def change(label, path, mutate, expected):
        document = json.loads(original[path])
        mutate(document)
        cases.append((label, {path: json.dumps(document).encode()}, expected))

    cases += [
        ('missing-texture', {'assets/poptartcore/textures/item/steel_ingot.png': None}, 'missing texture'),
        ('missing-model', {'assets/poptartcore/models/block/tin_block.json': None}, 'missing model'),
        ('missing-loot', {'data/poptartcore/loot_table/blocks/wooden_bucket.json': None}, 'Missing packaged resource'),
    ]
    change('wrong-name', 'assets/poptart_catalog/lang/en_us.json',
           lambda doc: doc.update({'item.poptartcore.steel_ingot': 'Wrong Name'}), 'translation|name disagrees')
    change('wrong-model-parent', 'assets/poptartcore/models/item/steel_ingot.json',
           lambda doc: doc.update(parent='minecraft:item/handheld'), 'Wrong item model parent')
    change('wrong-cube-texture', 'assets/poptartcore/models/block/tin_block.json',
           lambda doc: doc['textures'].update(all='poptartcore:block/lead_block'), 'Wrong cube model or texture')
    change('missing-tag-member', 'data/minecraft/tags/item/axes.json',
           lambda doc: doc['values'].remove('poptartcore:bronze_axe'), 'absent from its declared item tag')
    change('replaced-shared-tag', 'data/minecraft/tags/item/axes.json',
           lambda doc: doc.update(replace=True), 'replaces other mods')
    change('wrong-fuel', 'data/neoforge/data_maps/item/furnace_fuels.json',
           lambda doc: doc['values']['poptartcore:coal_coke'].update(burn_time=1), 'Wrong furnace fuel')
    change('wrong-packing', 'data/poptartcore/recipe/tin_block.json',
           lambda doc: doc['key']['#'].update(item='poptartcore:lead_ingot'), 'Wrong packing recipe')
    change('wrong-unpacking', 'data/poptartcore/recipe/tin_ingots_from_block.json',
           lambda doc: doc['result'].update(count=8), 'Wrong unpacking recipe')
    cases.append(('missing-tool-recipe', {'data/poptartcore/recipe/bronze_sword.json': None}, 'Missing packaged resource'))
    change('wrong-tool-ingredient', 'data/poptartcore/recipe/steel_pickaxe.json',
           lambda doc: doc['key']['I'].update(item='poptartcore:bronze_ingot'), 'Wrong crafting recipe')
    change('wrong-tool-count', 'data/poptartcore/recipe/steel_knife.json',
           lambda doc: doc['result'].update(count=2), 'Wrong crafting recipe')
    change('wrong-tool-output', 'data/poptartcore/recipe/steel_knife.json',
           lambda doc: doc['result'].update(id='poptartcore:bronze_knife'), 'Wrong crafting recipe')
    change('wrong-tool-pattern', 'data/poptartcore/recipe/bronze_sword.json',
           lambda doc: doc.update(pattern=['S', 'B', 'B']), 'Wrong crafting recipe')

    def rename_recipe_symbols(document):
        document['key']['X'] = document['key'].pop('I')
        document['pattern'] = [row.replace('I', 'X') for row in document['pattern']]

    change('tool-symbols-renamed', 'data/poptartcore/recipe/steel_axe.json', rename_recipe_symbols, None)
    change('tool-pattern-mirrored', 'data/poptartcore/recipe/bronze_axe.json',
           lambda doc: doc.update(pattern=[row[::-1] for row in doc['pattern']]), None)
    cases.append(('missing-primitive-recipe', {'data/poptartcore/recipe/bone_pick.json': None}, 'Missing packaged resource'))
    change('wrong-shapeless-ingredient', 'data/poptartcore/recipe/repeating_crossbow.json',
           lambda doc: doc['ingredients'][1].update(item='minecraft:redstone'), 'Wrong crafting recipe')
    change('extra-shapeless-ingredient', 'data/poptartcore/recipe/firestarter.json',
           lambda doc: doc['ingredients'].append({'item': 'minecraft:dirt'}), 'Wrong crafting recipe')
    change('shapeless-order-changed', 'data/poptartcore/recipe/firestarter.json',
           lambda doc: doc['ingredients'].reverse(), None)
    change('wrong-crossbow-pull-threshold', 'assets/poptartcore/models/item/repeating_crossbow.json',
           lambda doc: doc['overrides'][1]['predicate'].update({'poptartcore:pull': 0.8}), 'Wrong item model override predicate')
    change('missing-crossbow-firework-predicate', 'assets/poptartcore/models/item/repeating_crossbow.json',
           lambda doc: doc['overrides'][4]['predicate'].pop('poptartcore:firework'), 'Wrong item model override')

    def reorder_predicates(document):
        document['overrides'][1]['predicate'] = dict(reversed(list(document['overrides'][1]['predicate'].items())))

    change('crossbow-predicate-keys-reordered', 'assets/poptartcore/models/item/repeating_crossbow.json', reorder_predicates, None)
    change('missing-firestarter-tag', 'data/minecraft/tags/item/creeper_igniters.json',
           lambda doc: doc['values'].remove('poptartcore:firestarter'), 'absent from its declared item tag')
    cases.append(('invalid-unreferenced-json', {'assets/poptartcore/models/item/invalid_probe.json': b'{'}, 'JSON|ConvertFrom-Json'))

    def remove_fortune(document):
        for node in objects(document):
            if node.get('name') == 'poptartcore:raw_tin':
                node['functions'] = [entry for entry in node['functions'] if entry.get('function') != 'minecraft:apply_bonus']

    change('missing-fortune', 'data/poptartcore/loot_table/blocks/tin_ore.json', remove_fortune, 'Fortune or Silk Touch')

    def remove_silk(document):
        for node in objects(document):
            if node.get('name') == 'poptartcore:tin_ore':
                node['conditions'] = []

    change('missing-silk-touch', 'data/poptartcore/loot_table/blocks/tin_ore.json', remove_silk, 'Fortune or Silk Touch')

    def change_double_count(document):
        for node in objects(document):
            if node.get('function') == 'minecraft:set_count':
                node['count'] = 1

    change('wrong-double-slab', 'data/poptartcore/loot_table/blocks/clinker_brick_slab.json',
           change_double_count, 'Double slab loot')
    cases.append(('unexpected-no-loot-table', {
        'data/poptartcore/loot_table/blocks/iron_bloom.json': b'{"type":"minecraft:block","pools":[]}'
    }, 'No-loot block has a loot table'))
    change('duplicate-translation', 'assets/poptartcore/lang/en_us.json',
           lambda doc: doc.update({'item.poptartcore.steel_ingot': 'Steel Ingot'}), 'two owners')

    # Valid changes should pass too, including changing the owner of an English name.
    handwritten = json.loads(original['assets/poptartcore/lang/en_us.json'])
    generated = json.loads(original['assets/poptart_catalog/lang/en_us.json'])
    handwritten['item.poptartcore.steel_ingot'] = generated.pop('item.poptartcore.steel_ingot')
    cases.append(('translation-owner-moved', {
        'assets/poptartcore/lang/en_us.json': json.dumps(handwritten).encode(),
        'assets/poptart_catalog/lang/en_us.json': json.dumps(generated).encode(),
    }, None))
    change('additional-fuel-allowed', 'data/neoforge/data_maps/item/furnace_fuels.json',
           lambda doc: doc['values'].update({'minecraft:stick': {'burn_time': 100}}), None)

    # Preserve the special rules from the retired ring scripts, including their namespace scopes.
    language = 'assets/poptartcore/lang/en_us.json'
    rings = 'data/poptartcore/tags/item/equipment/rings.json'
    change('missing-ring-tooltip', language,
           lambda doc: doc.pop('item.poptartcore.rusted_iron_ring.desc'), 'Missing ring tooltip')
    change('missing-ring-tag', rings,
           lambda doc: doc['values'].remove('poptartcore:bulwark_ring'), 'absent.*tag|ring-slot tag')
    change('duplicate-ring-tag', rings,
           lambda doc: doc['values'].append('poptartcore:bulwark_ring'), 'duplicate ring-slot tag')
    change('wrong-ring-texture', 'assets/poptartcore/models/item/rusted_iron_ring.json',
           lambda doc: doc['textures'].update(layer0='poptartcore:item/dull_iron_ring'), 'Wrong equipment model texture')
    cases.append(('stale-ring-texture', {
        'assets/poptartcore/textures/item/bulwark_ring.png':
            original['assets/poptartcore/textures/item/bulwark_ring.png'] + b'\x00'
    }, 'Stale packaged equipment asset'))
    change('stale-ring-model', 'assets/poptartcore/models/item/bulwark_ring.json',
           lambda doc: doc.update(ambientocclusion=False), 'Stale packaged equipment asset')
    cases.append(('missing-repair-material-texture', {
        'assets/poptartcore/textures/item/bone_ash.png': None
    }, 'Missing packaged resource|missing texture'))

    def ring_recipe(item):
        return json.dumps({'type': 'minecraft:crafting_shapeless',
                           'ingredients': [{'item': item}],
                           'result': {'id': 'minecraft:stick', 'count': 1}}).encode()

    cases.extend([
        ('unexpected-ring-recipe-path', {'data/poptartcore/recipe/rusted_iron_ring.json':
                                        ring_recipe('minecraft:dirt')}, 'Unexpected equipment recipe'),
        ('unexpected-material-recipe-path', {'data/poptartcore/recipe/bone_ash.json':
                                            ring_recipe('minecraft:dirt')}, 'Unexpected equipment recipe'),
        ('unexpected-local-ring-recipe', {'data/poptartcore/recipe/ring_probe.json':
                                         ring_recipe('poptartcore:tiny_horseshoe')}, 'Unexpected equipment recipe reference'),
        ('unexpected-local-material-recipe', {'data/poptartcore/recipe/ring_probe.json':
                                             ring_recipe('poptartcore:bone_ash')}, 'Unexpected equipment recipe reference'),
        ('unexpected-third-party-ring-recipe', {'data/othermod/recipe/ring_probe.json':
                                               ring_recipe('poptartcore:ring_of_silence')}, 'Unexpected equipment recipe reference'),
        ('third-party-local-scope-allowed', {'data/othermod/recipe/ring_probe.json':
                                           ring_recipe('poptartcore:tiny_horseshoe')}, None),
        ('original-ring-reference-allowed', {'data/poptartcore/recipe/ring_probe.json':
                                           ring_recipe('poptartcore:rusted_iron_ring')}, None),
    ])
    change('missing-rarity-extension', 'META-INF/enumextensions.json',
           lambda doc: doc.update(entries=[]), 'malevolent rarity extension')
    change('wrong-rarity-extension', 'META-INF/enumextensions.json',
           lambda doc: doc['entries'][0]['parameters'].update(field='WRONG'), 'malevolent rarity extension')
    cases.append(('disabled-rarity-extension', {
        'META-INF/neoforge.mods.toml': original['META-INF/neoforge.mods.toml'].replace(
            b'enumExtensions=', b'unusedEnumExtensions=')
    }, 'rarity extension is not enabled'))
    change('additional-rarity-allowed', 'META-INF/enumextensions.json',
           lambda doc: doc['entries'].append(dict(doc['entries'][0], name='OTHER_RARITY')), None)

    tooltip_names = json.loads(original[language])
    generated_tooltips = json.loads(original['assets/poptart_catalog/lang/en_us.json'])
    key = 'item.poptartcore.rusted_iron_ring.desc'
    generated_tooltips[key] = tooltip_names.pop(key)
    cases.append(('tooltip-owner-moved', {
        language: json.dumps(tooltip_names).encode(),
        'assets/poptart_catalog/lang/en_us.json': json.dumps(generated_tooltips).encode(),
    }, None))

    references = output / 'reference-textures'
    for variant in ('matching', 'changed', 'missing'):
        directory = references / variant / 'rings'
        directory.mkdir(parents=True, exist_ok=True)
        for item, contract in manifest['equipmentAssets']['items'].items():
            source = directory / contract['referenceTexture']
            assert source.resolve().is_relative_to((references / variant).resolve())
            data = original[f'assets/poptartcore/textures/item/{item}.png']
            if variant == 'changed' and item == 'bulwark_ring':
                data += b'\x00'
            if variant == 'missing' and item == 'bulwark_ring':
                # This fixture path has never been created by the other variants.
                assert not source.exists(), 'Choose a fresh probe output directory for this missing fixture.'
                continue
            source.write_bytes(data)
    reference_args = {}
    for variant, error in [('matching', None), ('changed', 'Reference texture differs'),
                           ('missing', 'Missing reference texture')]:
        label = f'reference-textures-{variant}'
        cases.append((label, {}, error))
        reference_args[label] = ['-ReferenceRingTextures', str(references / variant / 'rings')]

    cases.append(('test-tag-shadow-detected', {}, 'Test tag shadows a real ring'))

    # Metal/storage blocks already use shared contracts. Preserve their remaining migration and wax rules.
    mixins = 'poptartcore.mixins.json'
    cases.append(('missing-mixin-config', {mixins: None}, 'Missing packaged resource'))
    for name in ('HoneycombWaxingMixin', 'IWaxableMoonlightMixin'):
        change(f'missing-{name}-registration', mixins,
               lambda doc, name=name: doc['mixins'].remove(f'wax.{name}'), 'Missing required mixin registration')
        cases.append((f'missing-{name}-class', {
            f'dev/poptartking/poptartcore/mixin/wax/{name}.class': None
        }, 'Missing required mixin class'))
    change('wrong-mixin-package', mixins,
           lambda doc: doc.update(package='incorrect.package'), 'Missing required mixin class')
    change('mixin-order-independent', mixins,
           lambda doc: doc['mixins'].reverse(), None)
    for kind, path in [
        ('blockstate', 'assets/poptartcore/blockstates/tin_ore.json'),
        ('block-model', 'assets/poptartcore/models/block/tin_ore.json'),
        ('item-model', 'assets/poptartcore/models/item/tin_ore.json'),
        ('loot', 'data/poptartcore/loot_table/blocks/tin_ore.json'),
    ]:
        change(f'obsolete-namespace-{kind}', path,
               lambda doc: doc.update(credit='wayfarer_core'), 'Obsolete resource namespace')
    change('non-namespace-credit-allowed', 'assets/poptartcore/models/block/tin_ore.json',
           lambda doc: doc.update(credit='Wayfarer'), None)
    change('missing-metal-pickaxe-tag', 'data/minecraft/tags/block/mineable/pickaxe.json',
           lambda doc: doc['values'].remove('poptartcore:lead_block'), 'absent from its declared block tag')
    change('missing-metal-iron-tool-tag', 'data/minecraft/tags/block/needs_iron_tool.json',
           lambda doc: doc['values'].remove('poptartcore:raw_silver_block'), 'absent from its declared block tag')
    change('wrong-storage-block-fuel', 'data/neoforge/data_maps/item/furnace_fuels.json',
           lambda doc: doc['values']['poptartcore:coal_coke_block'].update(burn_time=1), 'Wrong furnace fuel')
    cases.append(('missing-wax-item-model', {'assets/poptartcore/models/item/wax.json': None},
                  'Missing packaged resource|lacks item model'))
    cases.append(('missing-storage-recipe', {'data/poptartcore/recipe/bronze_block.json': None},
                  'Missing packaged resource'))
    change('wrong-wax-packing', 'data/poptartcore/recipe/wax_block.json',
           lambda doc: doc['key']['#'].update(item='minecraft:honeycomb'), 'Wrong packing recipe')

    for label, path in [
        ('rift-blockstate', 'assets/poptartcore/blockstates/sconce_soul_torch_lever.json'),
        ('rift-item-model', 'assets/poptartcore/models/item/rift_brazier.json'),
        ('quartz-item-model', 'assets/poptartcore/models/item/quartz_crystal.json'),
        ('magnet-item-model', 'assets/poptartcore/models/item/magnet_ring.json'),
        ('rubber-loot', 'data/poptartcore/loot_table/blocks/rubber_block.json'),
    ]:
        cases.append((f'missing-required-{label}', {path: None}, 'Missing required resource'))
    for label, path in [
        ('quartz', 'assets/poptartcore/textures/item/quartz_crystal.png'),
        ('magnet', 'assets/poptartcore/textures/item/magnet_ring.png'),
        ('rubber', 'assets/poptartcore/textures/block/rubber_block.png'),
    ]:
        cases.append((f'missing-wayfarer-{label}-texture', {path: None}, 'Missing packaged resource|missing texture'))

    helper = 'assets/poptartcore/models/block/sconce_lever/sconce_rift_torch_on_unlit.json'
    change('rift-helper-missing-parent', helper,
           lambda doc: doc.update(parent='poptartcore:block/absent_helper_probe'), 'missing model')
    unused = 'assets/poptartcore/models/block/rift_probe_unused.json'
    cases.append(('unreferenced-rift-helper-invalid-json', {unused: b'{not valid JSON'}, 'ConvertFrom-Json|JSON'))
    cases.append(('unreferenced-rift-helper-missing-model', {
        unused: b'{"parent":"poptartcore:block/absent_helper_probe"}'
    }, 'missing model'))
    cases.append(('unreferenced-rift-state-missing-model', {
        'assets/poptartcore/blockstates/rift_probe_unused.json':
            b'{"variants":{"":{"model":"poptartcore:block/absent_helper_probe"}}}'
    }, 'missing model'))
    simple_model = b'{"parent":"minecraft:block/cube_all","textures":{"all":"minecraft:block/stone"}}'
    cases.append(('valid-unreferenced-rift-helper-allowed', {unused: simple_model}, None))

    # A texture cannot satisfy a model reference, and a model cannot satisfy a texture reference.
    texture_only = json.loads(original[helper])
    texture_only['parent'] = 'poptartcore:block/rift_texture_only_probe'
    cases.append(('texture-cannot-replace-rift-model', {
        helper: json.dumps(texture_only).encode(),
        'assets/poptartcore/textures/block/rift_texture_only_probe.png':
            original['assets/poptartcore/textures/block/rubber_block.png'],
    }, 'missing model'))
    model_only = json.loads(original[helper])
    model_only.setdefault('textures', {})['probe'] = 'poptartcore:block/rift_model_only_probe'
    cases.append(('model-cannot-replace-rift-texture', {
        helper: json.dumps(model_only).encode(),
        'assets/poptartcore/models/block/rift_model_only_probe.json': simple_model,
    }, 'missing texture'))

    wayfarer_sources = output / 'wayfarer-reference-sources'
    mappings = manifest['wayfarerReferenceTextures']
    variants = [('matching', None, None)]
    for path in mappings:
        variants.extend([(f'changed-{Path(path).stem}', path, 'Reference texture differs'),
                         (f'missing-{Path(path).stem}', path, 'Missing reference texture')])
    for variant, target, error in variants:
        directory = wayfarer_sources / variant
        for path, relative in mappings.items():
            source = directory / relative
            assert source.resolve().is_relative_to(directory.resolve())
            if variant.startswith('missing-') and path == target:
                assert not source.exists(), 'Choose a fresh probe output directory for this missing fixture.'
                continue
            source.parent.mkdir(parents=True, exist_ok=True)
            data = original[path]
            if variant.startswith('changed-') and path == target:
                data += b'\x00'
            source.write_bytes(data)
        label = f'wayfarer-reference-{variant}'
        cases.append((label, {}, error))
        reference_args[label] = ['-ReferenceWayfarerSources', str(directory)]
    cases.append(('both-reference-roots-allowed', {}, None))
    reference_args['both-reference-roots-allowed'] = [
        '-ReferenceWayfarerSources', str(wayfarer_sources / 'matching'),
        '-ReferenceRingTextures', str(references / 'matching' / 'rings'),
    ]

    for label, path in [
        ('beekeeper-texture', 'assets/poptartcore/textures/armor/beekeeper_armor.png'),
        ('armor-stand-renderer', 'dev/poptartking/poptartcore/equipment/client/ArmorStandCapeLayer.class'),
        ('attachment-panel', 'assets/poptartcore/textures/gui/backpack/rucksack_attachments_panel.png'),
        ('bloomery-animation', 'assets/poptartcore/textures/block/bloomery/bloomery_front_lit.png.mcmeta'),
        ('smoker-sound', 'assets/poptartcore/sounds/bee_smoker/blow.ogg'),
    ]:
        cases.append((f'missing-feature-{label}', {path: None}, 'Missing required resource|missing texture'))
    change('missing-beekeeper-sting-mixin', 'poptartcore.mixins.json',
           lambda doc: doc['mixins'].remove('bee.BeeStingMixin'), 'Missing required mixin registration')
    change('missing-beekeeper-sleeve-mixin', 'poptartcore.mixins.json',
           lambda doc: doc['client'].remove('armor.FirstPersonBeekeeperSleeveMixin'), 'Missing required mixin registration')
    change('missing-smoker-use-mixin', 'poptartcore.mixins.json',
           lambda doc: doc['client'].remove('bee.BeeSmokerReequipMixin'), 'Missing required mixin registration')
    change('wrong-bloomery-state-count', 'assets/poptartcore/blockstates/bloomery.json',
           lambda doc: doc['variants'].pop(next(iter(doc['variants']))), 'Wrong feature resource')
    change('wrong-bloom-pile-state-count', 'assets/poptartcore/blockstates/iron_bloom.json',
           lambda doc: doc['variants'].pop(next(iter(doc['variants']))), 'Wrong feature resource')
    rucksack = 'assets/poptartcore/blockstates/rucksack.json'
    change('wrong-rucksack-upper-model', rucksack,
           lambda doc: doc['variants'][next(k for k in doc['variants'] if 'half=upper' in k)].update(model='poptartcore:block/rucksack'), 'Wrong feature resource')
    change('rucksack-key-order-independent', rucksack,
           lambda doc: doc.update(variants=dict(reversed(list(doc['variants'].items())))), None)
    spyglass = 'assets/poptartcore/models/item/backpack_attachments/spyglass.json'
    change('missing-spyglass-geometry', spyglass,
           lambda doc: doc.update(elements=[]), 'Wrong feature resource')
    change('missing-spyglass-head-transform', spyglass,
           lambda doc: doc['display'].pop('head'), 'Wrong feature resource')
    change('nonlocal-attachment-texture', spyglass,
           lambda doc: doc['textures'].update(probe='minecraft:block/stone'), 'Wrong feature resource')
    loot = 'data/poptartcore/loot_table/blocks/backpack.json'
    change('attachment-component-lost-on-drop', loot,
           lambda doc: doc['pools'][0]['entries'][0]['functions'][0]['include'].remove('poptartcore:backpack_attachments'), 'Wrong feature resource')
    change('extra-preserved-component-allowed', loot,
           lambda doc: doc['pools'][0]['entries'][0]['functions'][0]['include'].append('minecraft:custom_data'), None)
    cases.append(('forbidden-beekeeper-recipe', {'data/othermod/recipe/nested/beekeeper_helmet.json':
                                                ring_recipe('minecraft:dirt')}, 'Unexpected feature recipe'))
    change('wrong-portable-engine-recipe-tag', 'data/poptartcore/recipe/portable_engine.json',
           lambda doc: doc['key']['P'].update(tag='minecraft:logs'), 'Wrong feature resource')
    change('wrong-smoker-recipe', 'data/poptartcore/recipe/bee_smoker.json',
           lambda doc: doc['key']['C'].update(item='minecraft:dirt'), 'Wrong crafting recipe')
    change('wrong-smoker-sound-event', 'assets/poptartcore/sounds.json',
           lambda doc: doc['bee_smoker.blow'].update(sounds=[]), 'Wrong feature resource')
    cases.append(('missing-aeronautics-dependency-metadata', {'META-INF/neoforge.mods.toml':
                  original['META-INF/neoforge.mods.toml'].replace(b'modId="aeronautics"', b'modId="missing"')}, 'Missing required metadata'))

    workbench_sources = output / 'workbench-reference-sources'
    mappings = manifest['featureAssets']['referenceTextures']
    variants = [('matching', None, None)]
    for path in mappings:
        variants.extend([(f'changed-{Path(path).stem}', path, 'Reference texture differs'),
                         (f'missing-{Path(path).stem}', path, 'Missing reference texture')])
    for variant, target, error in variants:
        directory = workbench_sources / variant
        for path, relative in mappings.items():
            source = directory / relative
            assert source.resolve().is_relative_to(directory.resolve())
            if variant.startswith('missing-') and path == target:
                assert not source.exists(), 'Choose a fresh output directory for this missing reference.'
                continue
            source.parent.mkdir(parents=True, exist_ok=True)
            source.write_bytes(original[path] + (b'\x00' if variant.startswith('changed-') and path == target else b''))
        label = f'workbench-reference-{variant}'
        cases.append((label, {}, error))
        reference_args[label] = ['-ReferenceWorkbenchSources', str(directory)]
    cases.append(('all-reference-roots-allowed', {}, None))
    reference_args['all-reference-roots-allowed'] = [
        *reference_args['both-reference-roots-allowed'],
        '-ReferenceWorkbenchSources', str(workbench_sources / 'matching'),
    ]
    results = []
    checker = ROOT / 'scripts/verification/catalog/verify-packaged-catalog.ps1'

    def run_case(case):
        label, changes, expected = case
        probe = output / f'{label}.jar'
        with zipfile.ZipFile(probe, 'w', compression=zipfile.ZIP_DEFLATED) as archive:
            for path, data in (original | changes).items():
                if data is not None:
                    archive.writestr(path, data)
        probe_manifest = output / f'{label}.json'
        probe_data = copy.deepcopy(manifest)
        if label == 'test-tag-shadow-detected':
            probe_data['loadedRingTagMembers'].remove('poptartcore:bulwark_ring')
        probe_manifest.write_text(json.dumps(probe_data), encoding='utf-8')
        run = subprocess.run([args.powershell, '-NoLogo', '-NoProfile', '-File', str(checker),
                              '-JarPath', str(probe), '-CatalogManifest', str(probe_manifest),
                              *reference_args.get(label, [])],
                             cwd=ROOT, capture_output=True, encoding='utf-8', errors='replace')
        text = run.stdout + run.stderr
        passed = (run.returncode == 0) if expected is None else (
            run.returncode != 0 and re.search(expected, text, re.I) is not None)
        return {'case':label, 'passed':passed, 'exit':run.returncode, 'output':text.strip()}

    with ThreadPoolExecutor(max_workers=args.workers) as pool:
        for result in pool.map(run_case, cases):
            results.append(result)
            print(f'{"PASS" if result["passed"] else "FAIL"}: {result["case"]}', flush=True)
    (output / 'report.json').write_text(json.dumps(results, indent=2)+'\n', encoding='utf-8')
    failures = [result for result in results if not result['passed']]
    if failures:
        raise SystemExit(json.dumps(failures, indent=2))
    print(f'All {len(results)} catalog checker probes passed; the real JAR and sources were untouched.')


if __name__ == '__main__':
    main()
