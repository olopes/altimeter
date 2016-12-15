#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <termios.h>
#include <ctype.h>
#include <getopt.h>

#define TTY_DEVICE "/dev/ttyUSB1"
#define BUF_SIZE 4096
#define HEADER_SIZE 8
#define CMD_SIZE 7

// http://stackoverflow.com/questions/6947413/how-to-open-read-and-write-from-serial-port-in-c

int set_interface_attribs (int fd, int speed, int parity) {
    struct termios tty;
    memset (&tty, 0, sizeof tty);
    if (tcgetattr (fd, &tty) != 0)
    {
	perror ("error from tcgetattr");
	return -1;
    }

    cfsetospeed (&tty, speed);
    cfsetispeed (&tty, speed);

    tty.c_cflag = (tty.c_cflag & ~CSIZE) | CS8;     // 8-bit chars
    // disable IGNBRK for mismatched speed tests; otherwise receive break
    // as \000 chars
    tty.c_iflag &= ~IGNBRK;         // disable break processing
    tty.c_lflag = 0;                // no signaling chars, no echo,
    // no canonical processing
    tty.c_oflag = 0;                // no remapping, no delays
    tty.c_cc[VMIN]  = 0;            // read doesn't block
    tty.c_cc[VTIME] = 5;            // 0.5 seconds read timeout

    tty.c_iflag &= ~(IXON | IXOFF | IXANY); // shut off xon/xoff ctrl

    tty.c_cflag |= (CLOCAL | CREAD);// ignore modem controls,
    // enable reading
    tty.c_cflag &= ~(PARENB | PARODD);      // shut off parity
    tty.c_cflag |= parity;
    tty.c_cflag &= ~CSTOPB;
    tty.c_cflag &= ~CRTSCTS;

    if (tcsetattr (fd, TCSANOW, &tty) != 0)
    {
	perror ("error from tcsetattr");
	return -1;
    }
    return 0;
}

int set_blocking (int fd, int should_block) {
    struct termios tty;
    memset (&tty, 0, sizeof tty);
    if (tcgetattr (fd, &tty) != 0)
    {
	perror ("error from tggetattr");
	return -1;
    }

    tty.c_cc[VMIN]  = should_block ? 1 : 0;
    tty.c_cc[VTIME] = 5;            // 0.5 seconds read timeout

    if (tcsetattr (fd, TCSANOW, &tty) != 0) {
	perror ("error setting term attributes");
	return -1;
    }

    return 0;
}

void print_usage() {
    /* print usage */
    printf("Usage: fda-downloader [OPTIONS] <cmd>\n");
    printf("<cmd> is one of:\n");
    printf("    -u, --upload <file>   Retrieve contents from altimeter\n");
    printf("    -e, --erase           Erase altimeter contents\n");
    printf("    -s, --setup <rate>    Set altimeter sample rate in Hz.\n");
    printf("                          Possible values are: 1, 2, 4 or 8\n");
    printf("Options are:\n");
    printf("    -t, --tty <device>    Serial device to use.\n");
    printf("                          Defaults to /dev/ttyUSB1\n");
}

// command list
unsigned char cmd_upload[CMD_SIZE] = {0x0f, 0xda, 0x10, 0x00, 0xca, 0x00, 0x00};
unsigned char cmd_set1hz[CMD_SIZE] = {0x0f, 0xda, 0x10, 0x00, 0xcb, 0x00, 0x00};
unsigned char cmd_set2hz[CMD_SIZE] = {0x0f, 0xda, 0x10, 0x00, 0xcb, 0x00, 0x01};
unsigned char cmd_set4hz[CMD_SIZE] = {0x0f, 0xda, 0x10, 0x00, 0xcb, 0x00, 0x02};
unsigned char cmd_set8hz[CMD_SIZE] = {0x0f, 0xda, 0x10, 0x00, 0xcb, 0x00, 0x03};
unsigned char cmd_erased[CMD_SIZE] = {0x0f, 0xda, 0x10, 0x00, 0xcc, 0x00, 0x00};

int main(int argc, char** argv) {
    int fds;
    int fdf;
    unsigned int r, total, done, i, j;
    unsigned char *tty_cmd;
    unsigned char buf[BUF_SIZE], *pbuf = buf;
    struct termios options;
    char * tty_device=TTY_DEVICE;
    char * param="";
    int cmd_set = 0;
    char selected_cmd = '\0';

    while(1) {
	static struct option long_options[] =
        {
          /* These options don’t set a flag.
             We distinguish them by their indices. */
          {"upload",  required_argument, 0, 'u'},
          {"erase",   no_argument,       0, 'e'},
          {"setup" ,  required_argument, 0, 's'},
          {"tty",     required_argument, 0, 't'},
          {0, 0, 0, 0}
        };
	int c;
	/* getopt_long stores the option index here. */
	int option_index = 0;

	c = getopt_long (argc, argv, "u:es:t:", long_options, &option_index);
	
	/* Detect the end of the options. */
	if (c == -1)
	    break;

	switch(c) {
	case 'u':
	case 's':
	    param=optarg;
	case 'e':
	    cmd_set++;
	    selected_cmd=c;
	    break;
	case 't':
	    tty_device=optarg;
	    break;
	    
	case '?':
	    /* already handled? */
	    break;

	default:
	    return 1;
	}
    }
    
    if(cmd_set != 1 || optind < argc) {
	print_usage();
	return 1;
    }
    
    /* printf("Command: %c; Param: %s; TTY: %s\n", selected_cmd, param, tty_device); */

    /* this could be better written, but I don't care. :-P */

    /* decide what command should be sent to the altimeter */
    if(selected_cmd == 'e')
	tty_cmd=cmd_erased;
    else if(selected_cmd == 'u')
	tty_cmd=cmd_upload;
    else if(selected_cmd == 's' && !strcmp("1",param))
	tty_cmd=cmd_set1hz;
    else if(selected_cmd == 's' && !strcmp("2",param))
	tty_cmd=cmd_set2hz;
    else if(selected_cmd == 's' && !strcmp("4",param))
	tty_cmd=cmd_set4hz;
    else if(selected_cmd == 's' && !strcmp("8",param))
	tty_cmd=cmd_set8hz;
    

    /* open and configure tty device */
    fds = open(tty_device, O_RDWR | O_NOCTTY | O_NDELAY);
    if(fds == -1) {
	perror("Error opening tty device");
	return -1;
    }
    
    /* set speed to 19,200 bps, 8n1 (no parity) */
    if(set_interface_attribs (fds, B19200, 0))
	return 3;
    if(set_blocking (fds, 1)) /* set blocking */
	return 5;

    /* print command to be sent just for debug purposes * /
    printf("     ");
    for(i = 0; i < CMD_SIZE; i++)
	printf("0x%02x ", 0xff & tty_cmd[i]);
    putchar('\n'); */

    /* send request */
    r = write(fds, tty_cmd, CMD_SIZE);
    if(r == -1) {
	perror("Error sending cmd to TTY");
	close(fds);
	return -4;
    }
    r = tcflush(fds, TCOFLUSH);
    if(r == -1) {
        perror("Error sending cmd to TTY");
        close(fds);
        return -6;
    }

    // printf("Waiting...");
    /* wait for an answer... */
    usleep(250000); 

    // check answer
    i = 0;
    while(i < HEADER_SIZE) {
	r = read(fds, pbuf+i, HEADER_SIZE-i);
	if(r == -1) {
	    if(errno == EAGAIN) {
		errno = 0;
		usleep(25000);
		continue;
	    } else {
		perror("Could not read data from TTY");
		break;
	    }
	}
	i+=r;
    }
    if(i != HEADER_SIZE) {
	printf("Error condition detected, leaving...\n");
        close(fds);
        return -7;
    }

    /* print command to be sent just for debug purposes * /
    for(i = 0; i < HEADER_SIZE; i++)
	printf("0x%02x ", 0xff & buf[i]);
    putchar('\n'); */

    
    /* check signature */
    if(buf[0]!=0x07 || memcmp(pbuf+1, tty_cmd, CMD_SIZE)) {
	printf("Invalid signature header found: \n");
	close(fds);
	return -8;
    }


    if(selected_cmd == 'u') {
	int upl_header_size=HEADER_SIZE+4; /* extra bytes for data size */
	/* handle upload - send altimeter output to file */

	/* i is not set to zero in order to keep upload header as one */
	while(i < upl_header_size) {
	    r = read(fds, pbuf+i, upl_header_size-i);
	    if(r == -1) {
		if(errno == EAGAIN) {
		    errno = 0;
		    usleep(25000);
		    continue;
		} else {
		    perror("Could not read data from TTY");
		    break;
		}
	    }
	    i+=r;
	}
	
	total = ((buf[9]-2)*256 + buf[10])*256+buf[11]+upl_header_size;
	done=upl_header_size;
	/*printf("total bytes: %d\n", total); */

	if(total <= done) {
	    printf("No data available. Exiting...\n");
	} else {
	    fdf = open(param,O_RDWR|O_CREAT,S_IRUSR|S_IWUSR|S_IRGRP|S_IROTH);
	    if(fdf == -1) {
		perror("Error opening output file");
		close(fds);
		return -2;
	    }

	    /* write full header */
	    write(fdf, buf, upl_header_size);
    
	    while(done < total) {
		r = read(fds, buf, BUF_SIZE);
		if(r == -1) {
		    if(errno == EAGAIN) {
			errno=0;
			usleep(250000); // sleep and try again
			continue;
		    } else {
			perror("Error reading data from TTY");
			// TODO - check device not ready
			break;
		    }
		}
		if(r) write(fdf, buf, r);
		done += r;
		printf("%u/%u\n", done,total);
	    }
	    putchar('\n');
    
	    /* close the output file */
	    if(close(fdf) == -1) {
		/* error code goes here */
		perror("Error closing output file");
	    }
	}
    }
    
    /* close the serial port */
    if(close(fds) == -1) {
	/* error code goes here */
	perror("Error closing tty");
	return -11;
    }

    printf("Done!\n");

    return 0;
}
