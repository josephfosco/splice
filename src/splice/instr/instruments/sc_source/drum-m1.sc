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
SynthDef('drum-m1', {|
    freq = 440, vol = 0.4, pan = 0, attack = 0.01, release = 0.4,
	land= 0.9, gate = 1.0, done = 2, out = 0|

    var sound, env;

	env = EnvGen.kr(Env.perc(attackTime: attack, releaseTime: release),
		           gate: gate, doneAction: done);

	sound = SinOsc.ar(freq: Line.kr(start: freq, end: (freq * 0.5), dur: 0.5)) +
            SinOsc.ar(freq: freq) +
		    SinOsc.ar(freq: (freq / 2), phase: SinOsc.ar(freq: 1));

    sound = Pan2.ar((sound * env * vol), pan);

	OffsetOut.ar(out, sound);
}).add;
)

a=Synth("drum-m1", [\freq, 110])

plotTree(s)

// .writeDefFile("/home/joseph/src/clj/splice/src/splice/instr/instruments/sc/");
