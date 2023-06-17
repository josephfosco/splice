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
