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
SynthDef("woosh", {
	arg freq=73.416, vol=1, attack=3, release=3, gate=1.0, out=0;

	var noise, freq_env, lpf_env, hpf_env, vib_env;

	freq_env = EnvGen.kr(Env.perc(attackTime: attack, releaseTime: release, curve: [1, -2]),
		                 gate: gate, doneAction: 2);
	vib_env = EnvGen.kr(Env.perc(attackTime: 3, releaseTime: 2, curve: [1 -2]), gate: gate);
	// add 1 to freq_env to avoid click at start and end of envelope
	lpf_env = (freq_env * 5000 + 1) + (((SinOsc.kr(2.5) + 1) * 150) * vib_env);

	noise = HPF.ar(LPF.ar(WhiteNoise.ar, lpf_env), ((freq_env * -5000) + 5000)) * 0.5;

	Out.ar(out, [noise, noise]);  // sends the sound to 2 consecutive buses starting with the
                                  // the value of 'out'. In this case the sound will go out buses 0 and 1
}
)
).add;

a=Synth("woosh")

// .writeDefFile("/home/joseph/src/clj/splice/src/splice/instr/instruments/sc/");

a.set("gate", 0)

a.set("gate", 1)

a.set("done", Done.freeSelf)

a.free

plotTree(s)
g