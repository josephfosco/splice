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
SynthDef('pluck-string', {|
    freq = 164.81, vol = 0.5, pan = 0, done = 2, out = 0|

    var sound, env, dur;

	dur = 440 / freq;
	env = EnvGen.kr(Env.perc(attackTime: 0.00, releaseTime: dur),
		doneAction: done);


	sound = Pluck.ar(in: WhiteNoise.ar(), trig: 1, maxdelaytime: 1,
		             delaytime: (1 / freq), decaytime: dur);

    sound = Pan2.ar(sound, pan);

	OffsetOut.ar(out, (sound * env * vol));
}).add;
)
a=Synth("pluck-string")

plotTree(s)

// .writeDefFile("/home/joseph/src/clj/splice/src/splice/instr/instruments/sc/");
