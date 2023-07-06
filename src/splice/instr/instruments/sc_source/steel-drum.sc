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
SynthDef('steel-drum', {|
    freq = 440, vol = 1.0, pan = 0, gate = 1.0, done = 2, out = 0|

    var sound, env;

	env = EnvGen.kr(envelope: Env.perc(attackTime: 0.01, releaseTime: 0.5),
		           gate: gate, doneAction: done );

	sound = RLPF.ar(in: Saw.ar(freq: freq),
	                freq: (freq * 1.1),
		            rq: 0.4) + (SinOsc.ar(freq / 2));

    sound = Pan2.ar((sound * env * vol), pan);

	OffsetOut.ar(out, sound);
}).add;
)

a=Synth("steel-drum", [\freq, 440])

plotTree(s)

// .writeDefFile("/home/joseph/src/clj/splice/src/splice/instr/instruments/sc/");
