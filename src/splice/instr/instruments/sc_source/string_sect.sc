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
SynthDef("string-sect", {
	arg freq=440, vol=1, attack=0.3, sustain=1.0, release=0.3, gate=1.0,
	    done=2, out=0;

	var strings, env, sound;
	env = Env.asr(attackTime: attack, sustainLevel: sustain, releaseTime: release,
		          curve: [-3, 1, -2]);
	sound = LPF.ar((Mix.new([Pulse.ar(freq: freq, width: ((SinOsc.kr(3) * 0.4) + 0.5)),
		                     VarSaw.ar(freq: (freq * 1.01), width:0)]) * 0.3),
	               freq: 2000);

	strings = sound * EnvGen.kr(envelope: env, gate: gate, levelScale: vol, doneAction: done);
	Out.ar(out, [strings, strings]);  // sends the sound to 2 consecutive buses starting with the
                                      // the value of 'out'. In this case the sound will go out buses 0 and 1
}
)
).add;
a=Synth("string-sect", ["freq", 493.88])

a.set("gate", 0)

a.set("gate", 1)

// .writeDefFile("/home/joseph/src/clj/splice/src/splice/instr/instruments/sc/");

a.set("done", Done.freeSelf)

a.free

plotTree(s)
