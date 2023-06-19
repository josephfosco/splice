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
SynthDef('organ-m1', {|
    freq = 440, vol = 1.0, pan = 0, attack = 0.03, decay = 0.03, sustain = 0.4, release = 0.4,
	land= 0.9, gate = 1.0, done = 2, out = 0|

    var sound, env;

	env = EnvGen.kr(Env.adsr(attackTime: attack, decayTime: decay, sustainLevel: sustain,
		                     releaseTime: release),
		           gate: gate, doneAction: done);

	sound = Clip.ar(in: ((Pulse.ar(freq: freq) +
                          SinOsc.ar(freq: (freq * 3), phase: SinOsc.kr (freq: 6)) +
                          SinOsc.ar(freq: (freq / 0.5), phase: SinOsc.kr(freq: 3))) *
                         env *
                         SinOsc.ar(freq: (freq * 2))),
	                lo: (Line.kr(start: 1, end: land, dur: 16) * -1),
                    hi: Line.kr(start: 1, end: land, dur: 16));

    sound = Pan2.ar((sound * vol), pan);

	OffsetOut.ar(out, sound);
}).add;
)

a=Synth("organ-m1", [\freq, 440])

a.set("gate", 0)

plotTree(s)

// .writeDefFile("/home/joseph/src/clj/splice/src/splice/instr/instruments/sc/");
