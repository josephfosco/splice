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
SynthDef("plink-m1", {
	arg freq=440, vol=1, gate=1.0, done=2;

	var sound, env;

	env = EnvGen.kr(Env.perc(attackTime: 0.01, releaseTime: 0.3), gate: gate,
		            levelScale: (vol * 0.4), doneAction: done);

	sound = SinOsc.ar(freq: freq) +
		    SinOsc.ar(freq: (freq * 3)) * (1 / 3) +
		    SinOsc.ar(freq: (freq * 5.1)) * (1 / 5) +
	        SinOsc.ar(freq: (freq * 6.1)) * (1 / 6) +
		    SinOsc.ar(freq: (freq * 7.1)) * (1 / 8) +
		    SinOsc.ar(freq: (freq * 8)) * (1 / 8);

		OffsetOut.ar([0, 1], sound * env);
}
)
).add;

a=Synth("plink-m1")

a.set("gate", 0)

a.set("gate", 1)

a.set("done", Done.freeSelf)

a.free
