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
SynthDef("tst-sin", {
	arg freq=220, vol=1, gate=1.0, done=2, out=0;

	var sound, env;

	env = EnvGen.kr(Env.perc(attackTime: 0.5, releaseTime: 10.3), gate: gate,
		            levelScale: (vol * 0.8), doneAction: done);

	sound = SinOsc.ar(freq: freq) *
	        env;

	Out.ar(out, [sound, sound]);  // sends the sound to 2 consecutive buses starting with the
	                            // the value of 'out'. In this case the sound will go out buses 0 and 1
}
)
).add;

// .writeDefFile("/home/joseph/src/clj/splice/src/splice/instr/instruments/sc/");

a=Synth("tst-sin")

a.set("gate", 0)

a.set("gate", 1)

a.set("done", Done.freeSelf)

a.free
