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
SynthDef('pad-smooth', {|
    freq = 440, vol = 0.5, pan = 0, attack = 1.0, sustain = 1.0, release = 3.0,
	gate = 1.0, done = 2, out = 0 |

    var env, sound, output;

	env = Linen.kr(attackTime: attack, susLevel: sustain, releaseTime: release,
		           gate: gate, doneAction: done);

	sound = Mix.new([ SinOsc.ar(freq: freq, mul: 0.25),
		LPF.ar(in: Pulse.ar(freq: freq + (freq * 0.001), mul: 0.15), freq: freq)
	]) * vol;


    output = Pan2.ar((sound * env), pan);

	OffsetOut.ar(out, output);
}).add;
)

// .writeDefFile("/home/joseph/src/clj/splice/src/splice/instr/instruments/sc/");

a=Synth("pad-smooth", [\freq, 880])
a.set(\gate, 0.0)

// 432 A

a=Synth("pad-smooth", [\freq, 864])
b=Synth("pad-smooth", [\freq, 1296])
c=Synth("pad-smooth", [\freq, 1152])
d=Synth("pad-smooth", [\freq, 648])
e=Synth("pad-smooth", [\freq, 576])

// 528 A

a.set(\gate, 0.0)
b.set(\gate, 0.0)
c.set(\gate, 0.0)
d.set(\gate, 0.0)
e.set(\gate, 0.0)
