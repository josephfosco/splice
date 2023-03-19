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
SynthDef("reverb-2ch", {
	arg in1, in2, mix=1, room=0.5, damp=0.5, out=0;

	var verb;

	verb = FreeVerb.ar([in1, in2], mix, room, damp);

	Out.ar(out, verb) ;
}
)
).add;

// .writeDefFile("/home/joseph/src/clj/splice/src/splice/instr/instruments/sc/");

// a=Synth("gong")

// a.set("gate", 0)

// a.set("gate", 1)

// a.set("done", Done.freeSelf)

// a.free

// plotTree(s)
