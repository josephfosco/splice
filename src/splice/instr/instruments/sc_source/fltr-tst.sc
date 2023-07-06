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
SynthDef('fltr-tst', {|
    freq = 440, vol = 1.0, pan = 0, attack = 0.01, sustain = 0.3, release = 0.1,
	gate = 1.0, done = 2, out = 0, lpf_freq = 75, lpf_q = 0.3, lpf_freq_mult=370,
	lpf_lag_time = 0|

    var sound, env;

	env = Linen.kr(attackTime: attack, susLevel: sustain, releaseTime: release,
		            gate: gate, doneAction: done);

	/*
	sound = RLPF.ar(in: LFSaw.ar(freq: freq),
		freq: (SinOsc.ar(freq: lpf_freq, add: 1) * 500),
		rq: lpf_q
	);
	*/

	sound = RLPF.ar(in: Crackle.ar(chaosParam: 1.97),
		freq: (Lag.kr(in: LFNoise0.kr(freq: lpf_freq, add: 1) * lpf_freq_mult, lagTime: lpf_lag_time)),
		rq: lpf_q
	);

    sound = Pan2.ar((sound * env * vol), pan);

	OffsetOut.ar(out, sound);
}).add;
)

a=Synth("fltr-tst", [\freq, 440])

a.set("lpf_freq", 75)
a.set("lpf_q", 0.3)
a.set("lpf_freq_mult", 370)
a.set("lpf_lag_time", 0.0)
a.set("freq", 50)

a.set("gate", 0)

plotTree(s)

// .writeDefFile("/home/joseph/src/clj/splice/src/splice/instr/instruments/sc/");
