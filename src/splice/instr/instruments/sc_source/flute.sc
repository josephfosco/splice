(
SynthDef('flute', {|
    freq = 440, vol = 1.0, pan = 0, attack = 0.15, sustain = 1.0, release = 0.1,
	gate = 1.0, done = 2, out = 0|

    var sound, env;

	env = EnvGen.kr(Env.linen(attackTime: attack, sustainTime: sustain, releaseTime: release,
	                          curve: [-3, 1, -2]),
		            gate: gate, doneAction: done);


	sound = RLPF.ar(in: (LFSaw.ar(freq: freq) * 0.4),
		            freq: ((env * 60) + freq + (SinOsc.ar(3.5) * 40)),
		            rq: 0.7);


    sound = Pan2.ar((sound * env * vol), pan);

	OffsetOut.ar(out, sound);
}).add;
)
a=Synth("flute")

plotTree(s)

// .writeDefFile("/home/joseph/src/clj/splice/src/splice/instr/instruments/sc/");
