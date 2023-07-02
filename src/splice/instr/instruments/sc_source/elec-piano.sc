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
SynthDef('elec-piano', {|
    freq = 440, vol = 1.0, pan = 0, attack = 0.05, decay = 0.1, decay2 = 4, sustain = 0.05 release = 0.05,
	gate = 1.0, done = 2, out = 0|

    var sound, piano_env, env, filter_env, filter_envg;

	piano_env = Env.new(levels:[0, 1, 0.8, sustain, 0], times: [attack, decay, decay2, release], releaseNode: 3);
	env = EnvGen.kr(envelope: piano_env,
		            gate: gate, doneAction: done);
	filter_env = Env.new(levels:[2500, 2000, 1800, 200, 0], times: [attack, decay, decay2, release], releaseNode: 3);
	filter_envg = EnvGen.kr(envelope: filter_env,
		            gate: gate, doneAction: done);

    sound = (Pulse.ar(freq: freq, width: 0.5 + (env * 0.3)) * 0.5) + (Pulse.ar(freq: freq) * 0.5);

    // RLPF.ar(in:sound, freq: 0.5 + (((env *-1) + 1)* 500)));
	sound = RLPF.ar(in:sound, freq: filter_envg);

    sound = Pan2.ar((sound * env * vol), pan);

	OffsetOut.ar(out, sound);
}).add;
)

a=Synth("elec-piano", [\freq, 440])
a.set("gate", 0)

plotTree(s)

// .writeDefFile("/home/joseph/src/clj/splice/src/splice/instr/instruments/sc/");

(
Pbind(
	\instrument, "elec-piano",
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


(
SynthDef(\rhodey_sc, {
    |
    // standard meanings
    out = 0, freq = 440, gate = 1, pan = 0, amp = 0.1,
    // all of these range from 0 to 1
    vel = 0.8, modIndex = 0.2, mix = 0.2, lfoSpeed = 0.4, lfoDepth = 0.1
    |
    var env1, env2, env3, env4;
    var osc1, osc2, osc3, osc4, snd;

    lfoSpeed = lfoSpeed * 12;

    freq = freq * 2;

    env1 = EnvGen.ar(Env.adsr(0.001, 1.25, 0.0, 0.04, curve: \lin));
    env2 = EnvGen.ar(Env.adsr(0.001, 1.00, 0.0, 0.04, curve: \lin));
    env3 = EnvGen.ar(Env.adsr(0.001, 1.50, 0.0, 0.04, curve: \lin));
    env4 = EnvGen.ar(Env.adsr(0.001, 1.50, 0.0, 0.04, curve: \lin));

    osc4 = SinOsc.ar(freq * 0.5) * 2pi * 2 * 0.535887 * modIndex * env4 * vel;
    osc3 = SinOsc.ar(freq, osc4) * env3 * vel;
    osc2 = SinOsc.ar(freq * 15) * 2pi * 0.108819 * env2 * vel;
    osc1 = SinOsc.ar(freq, osc2) * env1 * vel;
    snd = Mix((osc3 * (1 - mix)) + (osc1 * mix));
    snd = snd * (SinOsc.ar(lfoSpeed) * lfoDepth + 1);

    // using the doneAction: 2 on the other envs can create clicks (bc of the linear curve maybe?)
    snd = snd * EnvGen.ar(Env.asr(0, 1, 0.1), gate, doneAction: 2);
    snd = Pan2.ar(snd, pan, amp);

    Out.ar(out, snd);
}).add;
)

(
Pbind(
    \instrument, \rhodey_sc,
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
