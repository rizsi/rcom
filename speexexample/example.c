// Build: $ gcc example.c -lspeexdsp

#include <speex/speex_echo.h>
#include "speex/speex_preprocess.h"
#include <unistd.h>
#include <stdio.h>

int main(int argc, char ** argv)
{
	int frame_size=256;
	int sample_size=2;
	int tail_length=1024;
	int sampleRate = 8000;
	// The frame recorded
	spx_int16_t input_frame[frame_size];
	// The frame played
	spx_int16_t echo_frame[frame_size];
	// The frame processed
	spx_int16_t output_frame[frame_size];
	SpeexEchoState * echo_state = speex_echo_state_init(frame_size, tail_length);
	speex_echo_ctl(echo_state, SPEEX_ECHO_SET_SAMPLING_RATE, &sampleRate);
	SpeexPreprocessState *den=speex_preprocess_state_init(frame_size, sampleRate);
	speex_preprocess_ctl(den, SPEEX_PREPROCESS_SET_ECHO_STATE, echo_state);
	while(1)
	{
		/*for(int i=0;i<frame_size;++i)
		{
			fprintf(stderr, "data %d: %d\n", i, (int)input_frame[i]);
		}*/
		read(STDIN_FILENO, (char *) input_frame, frame_size*sample_size);
		read(STDIN_FILENO, (char *) echo_frame, frame_size*sample_size);
		//fprintf(stderr, "Echo frame read\n");
		speex_echo_cancellation(echo_state, input_frame, echo_frame, output_frame);
		speex_preprocess_run(den, output_frame);
		write(STDOUT_FILENO, (char *) output_frame, frame_size*sample_size);
		//fprintf(stderr, "Cancelled written\n");
	}
}

