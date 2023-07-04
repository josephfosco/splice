//    Copyright (C) 2023  Joseph Fosco. All Rights Reserved
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

(
SynthDef('wurlitzer-piano', {|
    freq = 440, vol = 1.0, pan = 0, attack = 0.01, decay = 0.1, decay2 = 2.5, sustain = 0.0,
	release = 0.05, vel = 0.7, gate = 1.0, done = 2, out = 0|

    var sound, piano_env, piano_envg, filter_env, filter_envg;

	piano_env = Env.new(levels:[0, 1, 0.8, sustain, 0], times: [attack, decay, decay2, release], releaseNode: 3);
	piano_envg = EnvGen.kr(envelope: piano_env,
		            gate: gate, doneAction: done);
	filter_env = Env.new(levels:[9.0, 6.0, 5.0, 0.05, 0.05], times: [attack, decay, decay2, release],
		                 curve: [0, 0, -3, 0], releaseNode: 3);
	filter_envg = EnvGen.kr(envelope: filter_env,
		            gate: gate, doneAction: done);

    sound = (Pulse.ar(freq: freq, width: 0.5 + (piano_envg * 0.3)) * 0.4) + (Pulse.ar(freq: freq) * 0.4);
 	sound = RLPF.ar(in:sound, freq: (freq * 0.7 * vel) * filter_envg);

	sound = Pan2.ar((sound * piano_envg * (vol * 0.7)), pan);
	OffsetOut.ar(out, sound);
}).add;
)

a=Synth("wurlitzer-piano", [\freq, 440, \vel, 0.7])
a.set("gate", 0)

plotTree(s)

// .writeDefFile("/home/joseph/src/clj/splice/src/splice/instr/instruments/sc/");

(
Pbind(
	\instrument, "wurlitzer-piano",
    \scale, Scale.mixolydian,
    \octave, 4,
    \root, 2,
    \legato, Pseq([0.9, 0.5, 0.5, 0.9, 0.9, 0.9, 0.9, 0.5, 1, 0.5, 1, 0.6, 0.3], inf),
    \dur, Pseq([1 + (1/3), 1/3, 1/3, 1/7, 6/7, 5/6, 1/6, 1/2, 2/6, 1/6, 2 + 1/2, 1, 1/2], inf),
    \degree, Pseq([
        [0, 2, 4], 2, 4, 7, 8, 7, 0, [1, 3, 6], 5, [1, 3, 6], Rest(), [-1, 1, 3], [1, 3, 5],
        [0, 2, 4], 2, 4, 8, 9, 7, 0, [1, 3, 6], 5, [1, 3, 6], Rest(), [-1, 1, 3], [1, 3, 5],
    ], inf),
    \mix, 0.2,
    \modIndex, 0.2,
    \lfoSpeed, 0.5,
    \lfoDepth, 0.4,
    \vel, Pgauss(0.8, 0.1, inf),
    \amp, 0.3
).play(TempoClock(1.5));
)
