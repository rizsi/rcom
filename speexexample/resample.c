// Build: $ gcc resample.c -lspeexdsp

#include <speex/speex_resampler.h>
#include "speex/speex_preprocess.h"
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

int readFully(int fd, char * buf, size_t count)
{
	while(count>0)
	{
		ssize_t n=read(fd, buf, count);
		if(n<0)
		{
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

int main(int argc, char ** argv)
{
	int frame_size=256;
	int sample_size=2;
	int sampleRate = 8000;
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
		if(readFully(STDIN_FILENO, (char *) input_frame, frame_size*sample_size))
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
		in_len=frame_size;
		out_len=frame_size;
		do
		{
			err=speex_resampler_process_int(resampler_state, // SpeexResamplerState *st, 
		                         0, //spx_uint32_t channel_index, 
		                         input_frame, // const spx_int16_t *in, 
		                         &in_len, //spx_uint32_t *in_len, 
		                         output_frame, //spx_int16_t *out, 
		                         &out_len //spx_uint32_t *out_len
					);
			checkError(err);
			if(out_len>0)
			{
				write(STDOUT_FILENO, (char *) output_frame, out_len*sample_size);
			}
		}while(in_len==0);
	}
}

