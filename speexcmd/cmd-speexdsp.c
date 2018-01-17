// Build: $ gcc -o speexcmd cmd-speexdsp.c -lspeexdsp

#include <speex/speex_resampler.h>
#include <speex/speex_echo.h>
#include <speex/speex_preprocess.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>

// TODO remove this section - in theory it does nothing in current mingw versions
#ifdef WIN32
//#include <stdbin.h>
#warning Win32!
#include <fcntl.h>
#undef _fmode
int _fmode = _O_BINARY;
//int _CRT_fmode = _O_BINARY;
#endif

int frame_size;
int sampleRate;
int level=0;

int writeFully(char * buf, size_t count)
{
	if(level)
	{
		fprintf(stderr, "WRITEFULLY query: %d\n", (int)count);
		fflush(stderr);
	}
	int origN=count;
	while(count>0)
	{
		int n=fwrite(buf, 1, count, stdout);
		if(n!=count)
		{
			if(feof(stdout)||ferror(stdout)||n<=0)
			{
				fprintf(stderr, "WRITEFULLY error %d %d %d\n", feof(stdin), ferror(stdin), n);
				fflush(stderr);
				exit(-1);
				return 1;
			}else
			{
				if(level)
				{
					fprintf(stderr, "WRITEFULLY: EAGAIN %d\n", (int)n);
					fflush(stderr);
				}
			}
		}
		count-=n;
		buf+=n;
		if(level)
		{
			fprintf(stderr, "WRITEFULLY: written n bytes %d\n", (int)n);
			fflush(stderr);
		}
	}
	if(level)
	{
		fprintf(stderr, "WRITEFULLY finished: %d\n", origN);
		fflush(stderr);
	}
}

int readFully(char * buf, size_t count)
{
	if(level)
	{
		fprintf(stderr, "READFULLY query: %d\n", (int)count);
		fflush(stderr);
	}
	int origN=count;
	while(count>0)
	{
		int n=fread(buf, 1, count, stdin);
		if(n!=count)
		{
			if(feof(stdin)||ferror(stdin)||n<=0)
			{
				fprintf(stderr, "READFULLY error %d %d %d\n", feof(stdin), ferror(stdin), n);
				fflush(stderr);
				exit(-1);
				return 1;
			}else
			{
				if(level)
				{
					fprintf(stderr, "READFULLY: EAGAIN %d\n", (int)n);
					fflush(stderr);
				}
			}
		}
		count-=n;
		buf+=n;
		if(level)
		{
			fprintf(stderr, "READFULLY: read n bytes %d\n", (int)n);
			fflush(stderr);
		}
	}
	if(level)
	{
		fprintf(stderr, "READFULLY finished: %d\n", (int)origN);
		fflush(stderr);
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
		if(readFully((char *) &in_len, 4))
		{
			return 1;
		}
		if(readFully((char *) input_frame, in_len*sample_size))
		{
			return 1;
		}
		if(readFully((char *) &sourceHzNew, 4))
		{
			return 1;
		}
		if(readFully((char *) &targetHzNew, 4))
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
		if(level)
		{
			fprintf(stderr, "Data received... %d\n", in_len);
			fflush(stderr);
		}
		err=speex_resampler_process_int(resampler_state, // SpeexResamplerState *st, 
	                         0, //spx_uint32_t channel_index, 
	                         input_frame, // const spx_int16_t *in, 
	                         &in_len, //spx_uint32_t *in_len, 
	                         output_frame, //spx_int16_t *out, 
	                         &out_len //spx_uint32_t *out_len
				);
		checkError(err);
		if(writeFully((char *)&in_len, 4))
		{
			return 1;
		}
		if(writeFully((char *)&out_len, 4))
		{
			return 1;
		}
		//write(STDOUT_FILENO, , 4);
		//write(STDOUT_FILENO, (char *)&out_len, 4);
		if(out_len>0)
		{
			//write(STDOUT_FILENO, (char *) output_frame, out_len*sample_size);
			if(writeFully((char *) output_frame, out_len*sample_size))
			{
				return 1;
			}
		}
		fflush(stdout);
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
		readFully((char *) input_frame, frame_size*sample_size);
		readFully((char *) echo_frame, frame_size*sample_size);
		speex_echo_cancellation(echo_state, input_frame, echo_frame, output_frame);
		speex_preprocess_run(den, output_frame);
		writeFully((char *) output_frame, frame_size*sample_size);
		fflush(stdout);
	}
}

int main(int argc, char ** argv)
{
#ifndef WIN32
//	freopen(NULL, "wb", stderr);
//	fprintf(stderr, "error returned by freopen stderr was %d\n", errno);
	freopen(NULL, "rb", stdin);
	fprintf(stderr, "error returned by freopen stdin was %d\n", errno);
	freopen(NULL, "wb", stdout);
	fprintf(stderr, "error returned by freopen stdout was %d\n", errno);
	fflush(stderr);
#endif
#ifdef WIN32
	setmode(fileno(stdin), O_BINARY);
	setmode(fileno(stdout), O_BINARY);
	setmode(fileno(stderr), O_BINARY);
#endif
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
