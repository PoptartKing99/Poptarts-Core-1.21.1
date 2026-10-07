"""Report 10 ms RMS peaks from a mono PCM WAV heartbeat recording."""
import array
import math
import sys
import wave

with wave.open(sys.argv[1], "rb") as audio:
    assert audio.getnchannels() == 1 and audio.getsampwidth() == 2
    rate = audio.getframerate()
    samples = array.array("h", audio.readframes(audio.getnframes()))
if sys.byteorder != "little":
    samples.byteswap()
step = rate // 100
envelope = [math.sqrt(sum(v * v for v in samples[i:i + step]) / step)
            for i in range(0, len(samples) - step, step)]
threshold = max(envelope) * 0.45
candidates = [i for i in range(1, len(envelope) - 1)
              if envelope[i] > threshold
              and envelope[i] >= envelope[i - 1]
              and envelope[i] > envelope[i + 1]]
peaks = []
for i in sorted(candidates, key=lambda i: envelope[i], reverse=True):
    if all(abs(i - other) >= 18 for other in peaks):
        peaks.append(i)
for i in sorted(peaks):
    print(f"peak={i / 100:.2f}s tick={i / 5:.2f} rms={envelope[i]:.0f}")
