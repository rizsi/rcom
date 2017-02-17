// Build: $ gcc -o speexcmd cmd-speexdsp.c -lspeexdsp

#include <speex/speex_resampler.h>
#include <speex/speex_echo.h>
#include <speex/speex_preprocess.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int frame_size;
int sampleRate;

int readFully(int fd, char * buf, size_t count)
{
	while(count>0)
	{
		ssize_t n=read(fd, buf, count);
		if(n<=0)
		{
			fprintf(stderr, "Read fully error: %d\n", (int)n);
			return -1;
		}
		count-=n;
		buf+=n;
	}
	return 0;
}

void checkError(int err)
{
	if(err!=0)
	{
		fprintf(stderr, "Resampler error: %d %s", err, speex_resampler_strerror(err));
		exit(-1);
		return;
	}
}

int resample()
{
	int sample_size=2;
	spx_uint32_t sourceHz=sampleRate;
	spx_uint32_t targetHz=sampleRate;
	spx_uint32_t sourceHzNew;
	spx_uint32_t targetHzNew;
	int err;
	int in_len;
	int out_len;
	// The original frame
	spx_int16_t input_frame[frame_size];
	// The frame processed
	spx_int16_t output_frame[frame_size];
	// Resampler is initialized to 8000 Hz source as we have no information
	// about clock skew when the stream playback is started.
	SpeexResamplerState * resampler_state=speex_resampler_init( 2, //spx_uint32_t nb_channels, 
                                          sampleRate, //spx_uint32_t in_rate, 
                                          sampleRate, //spx_uint32_t out_rate, 
                                          10, // int quality [0,10] 10 is best,
                                          &err// int *err
					);
	checkError(err);
	while(1)
	{
		if(readFully(STDIN_FILENO, (char *) &in_len, 4))
		{
			return 1;
		}
		if(readFully(STDIN_FILENO, (char *) input_frame, in_len*sample_size))
		{
			return 1;
		}
		if(readFully(STDIN_FILENO, (char *) &sourceHzNew, 4))
		{
			return 1;
		}
		if(readFully(STDIN_FILENO, (char *) &targetHzNew, 4))
		{
			return 1;
		}
		if(sourceHzNew!=sourceHz || targetHzNew!=targetHz)
		{
			sourceHz=sourceHzNew;
			targetHz=targetHzNew;
			err=speex_resampler_set_rate(resampler_state, 
                              sourceHz, 
                              targetHz);
			checkError(err);
		}
		out_len=frame_size;
		//fprintf(stderr, "Data received... %d\n", in_len);
		err=speex_resampler_process_int(resampler_state, // SpeexResamplerState *st, 
	                         0, //spx_uint32_t channel_index, 
	                         input_frame, // const spx_int16_t *in, 
	                         &in_len, //spx_uint32_t *in_len, 
	                         output_frame, //spx_int16_t *out, 
	                         &out_len //spx_uint32_t *out_len
				);
		checkError(err);
		write(STDOUT_FILENO, (char *)&in_len, 4);
		write(STDOUT_FILENO, (char *)&out_len, 4);
		if(out_len>0)
		{
			write(STDOUT_FILENO, (char *) output_frame, out_len*sample_size);
		}
	}
}

void cancelecho()
{
	int sample_size=2;
	int tail_length=1024;
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
		read(STDIN_FILENO, (char *) input_frame, frame_size*sample_size);
		read(STDIN_FILENO, (char *) echo_frame, frame_size*sample_size);
		speex_echo_cancellation(echo_state, input_frame, echo_frame, output_frame);
		speex_preprocess_run(den, output_frame);
		write(STDOUT_FILENO, (char *) output_frame, frame_size*sample_size);
	}
}

int main(int argc, char ** argv)
{
	printf("speexcmd 0.0.5 for RCOM:");
	fflush(stdout);
        if(argc<2)
        {
            fprintf(stderr, "speexcmd - Missing first arg: program to execute: resample or cancelecho\n");
            exit(1);
        }
        if(argc<3)
        {
            fprintf(stderr, "speexcmd - Missing second arg: frame size in samples\n");
            exit(1);
        }
        if(argc<4)
        {
            fprintf(stderr, "speexcmd - Missing third arg: samplerate\n");
            exit(1);
        }
        frame_size=atoi(argv[2]);
        fprintf(stderr, "speexcmd - Frame size in samples: %d\n", frame_size);
        if(frame_size<1)
        {
            fprintf(stderr, "speexcmd - Frame size must be positive\n");
            exit(1);
        }
        sampleRate=atoi(argv[3]);
        fprintf(stderr, "speexcmd - Sample rate: %d\n", sampleRate);
        if(sampleRate<3500)
        {
            fprintf(stderr, "speexcmd - Sample rate must be more than 3499\n");
            exit(1);
        }
        if(!strcmp("resample", argv[1]))
        {
            fprintf(stderr, "speexcmd - Resampler started...\n");
            resample();
            return 0;
        } else if(!strcmp("cancelecho", argv[1]))
        {
            fprintf(stderr, "speexcmd - Cancel echo\n");
	    cancelecho();
            return 0;
        }else
        {
            fprintf(stderr, "speexcmd - Error: program to execute\n");
            exit(1);
            return 1;
        }
}
