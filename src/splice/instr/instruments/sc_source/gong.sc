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
SynthDef("gong", {
	arg freq=73.416, vol=1, gate=1.0, done=2;

	var fltr_noise, gong, gong_sound, env, freq_env, vib_env;

	env = EnvGen.kr(Env.perc(attackTime: 0.04, releaseTime: 8, curve: [-3, -2]), gate: gate,
	                doneAction: done);
	vib_env = EnvGen.kr(Env.perc(attackTime: 3, releaseTime: 3), gate: gate, levelScale: 0.5);
	freq_env = EnvGen.kr(Env.perc(attackTime: 1.5, releaseTime: 6, curve: [2, 2]), gate: gate);

 	fltr_noise = HPF.ar(LPF.ar(PinkNoise.ar(), ((1 + (freq_env * 7000)) +
		                                        (((SinOsc.kr(2.5) + 1.8) * 300) * vib_env))),
                       ((freq_env * -5000) + 5000)) * 0.03;
	gong_sound = LPF.ar(((LFTri.ar(freq: freq) * (LFTri.ar(freq: (freq * 1.5)))) +
		                  SinOsc.ar(freq: freq)),
	                   freq: 1400);

	gong = (gong_sound + fltr_noise) * ((env + ((SinOsc.kr(freq: 3) * vib_env) * 0.15)));

	OffsetOut.ar([0, 1], (gong * vol));
}
)
).add;


a=Synth("gong")

a.set("gate", 0)

a.set("gate", 1)

a.set("done", Done.freeSelf)

a.free

plotTree(s)
