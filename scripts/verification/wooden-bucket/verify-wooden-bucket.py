"""Check the source and packaged bucket for missing exterior faces and a clear opening."""
import json
from pathlib import Path
import zipfile

root = Path(__file__).resolve().parents[3]
jar = max((p for p in (root / 'build/libs').glob('*.jar')
           if 'sources' not in p.name and 'javadoc' not in p.name), key=lambda p: p.stat().st_mtime)
with zipfile.ZipFile(jar) as archive:
    assert 'assets/poptartcore/models/block/wooden_bucket.json' not in archive.namelist()
    models = (
        ('centered-bucket', 0, (root / 'scripts/assets/wooden-bucket/centered-bucket.json').read_bytes()),
        ('wooden_bucket_flush', -3, archive.read('assets/poptartcore/models/block/wooden_bucket_flush.json')),
    )
    for name, dz, model_data in models:
        model = json.loads(model_data)
        # Every visible face uses one atlas pixel per model unit.
        for element in model['elements']:
            size = [b-a for a,b in zip(element['from'],element['to'])]
            for face, data in element['faces'].items():
                axes = {'north': (0,1), 'south': (0,1), 'east': (2,1),
                        'west': (2,1), 'up': (0,2), 'down': (0,2)}[face]
                uv = data['uv']
                pixels = (abs(uv[2]-uv[0])*2, abs(uv[3]-uv[1])*2)
                assert pixels == tuple(size[i] for i in axes), (name, face, 'stretched UV', pixels, size)
        def covered(face, point):
            axis = {'east': 0, 'west': 0, 'up': 1, 'down': 1, 'north': 2, 'south': 2}[face]
            upper = face in ('east', 'up', 'south')
            for element in model['elements']:
                low, high = element['from'], element['to']
                if face not in element['faces'] or point[axis] != (high if upper else low)[axis]:
                    continue
                if all(low[i] < point[i] < high[i] for i in range(3) if i != axis):
                    return True
            return False
        for y in range(14):
            for t in range(10):
                for face, point in (
                    ('north', (3.5+t, y+.5, 3+dz)),
                    ('south', (3.5+t, y+.5, 13+dz)),
                    ('west', (3, y+.5, 3.5+t+dz)),
                    ('east', (13, y+.5, 3.5+t+dz)),
                ):
                    assert covered(face, point), (name, face, point)
        for x in range(4,12):
            for z in range(4,12):
                assert not covered('up', (x+.5,14,z+.5+dz)), (name, 'closed opening')
                assert covered('up', (x+.5,2,z+.5+dz)), (name, 'missing floor')
print('PASS: source and placed bucket models have complete walls, open tops, recessed floors and unstretched UVs')

with zipfile.ZipFile(jar) as archive:
    states = json.loads(archive.read('assets/poptartcore/blockstates/wooden_bucket.json'))['variants']
    assert len(states) == 16
    for direction in ('north','east','south','west'):
        assert states[f'half=lower,facing={direction},open=false']['model'] == 'poptartcore:block/wooden_bucket_flush'
        assert states[f'half=lower,facing={direction},open=true']['model'] == 'poptartcore:block/wooden_bucket_flush'
        assert states[f'half=upper,facing={direction},open=false']['model'].endswith('tap_closed')
        assert states[f'half=upper,facing={direction},open=true']['model'].endswith('_tap')
    for name in ('resin_layer','maple_syrup_layer'):
        path = f'assets/poptartcore/textures/block/fluid/{name}.png'
        assert archive.read(path) == (root / 'src/main/resources' / path).read_bytes()
    assert 'dev/poptartking/poptartcore/decoration/TreeTapperBlockEntity.class' in archive.namelist()
print('PASS: open/closed models, fluid textures and tank class packaged')

with zipfile.ZipFile(jar) as archive:
    for name in ('tapper_resin_drip','tapper_syrup_drip'):
        data = json.loads(archive.read(f'assets/poptartcore/particles/{name}.json'))
        assert data['textures'] == ['poptartcore:tapper_drip_hang', 'poptartcore:tapper_drip_fall', 'poptartcore:tapper_drip_land']
        for stage in ('drip_hang', 'drip_fall', 'drip_land'):
            assert f'assets/poptartcore/textures/particle/tapper_{stage}.png' in archive.namelist()
    assert 'dev/poptartking/poptartcore/decoration/client/TapperDripParticle.class' in archive.namelist()
print('PASS: drip particle definitions and provider class packaged')
