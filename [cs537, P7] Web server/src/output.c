#include "helper.h"
#include <sys/time.h>
#include <assert.h>
#include <unistd.h>

double spinfor = 5.0;

void getargs() {
    char *buf, *p;

    /* Extract the four arguments */
    if ((buf = getenv("QUERY_STRING")) != NULL) {
        p = strtok(buf, "&");
        if (p == NULL) return;
        spinfor = (double)atoi(p);
        return;
    }
}

double Time_GetSeconds() {
    struct timeval t;
    int rc = gettimeofday(&t, NULL);
    assert(rc == 0);
    return (double)((double)t.tv_sec + (double)t.tv_usec / 1e6);
}

/**
 * @brief Code for a CGI program. Basically, it spins for a fixed amount of
 time, which you may useful in testing various aspects of your server.
 *
 * use it to test that you are correctly having multiple threads handling http
 requests.
 *
 * @param argc
 * @param argv
 * @return int
 */
int main(int argc, char *argv[]) {
    char content[MAXBUF];

    getargs();

    double t1 = Time_GetSeconds();
    while ((Time_GetSeconds() - t1) < spinfor) sleep(1);
    double t2 = Time_GetSeconds();

    /* Make the response body */
    sprintf(content, "<p>Welcome to the CGI program</p>\r\n");
    sprintf(content,
            "%s<p>My only purpose is to waste time on the server!</p>\r\n",
            content);
    sprintf(content, "%s<p>I spun for %.2f seconds</p>\r\n", content, t2 - t1);

    /* Generate the HTTP response */
    printf("Content-length: %lu\r\n", strlen(content));
    printf("Content-type: text/html\r\n\r\n");
    printf("%s", content);
    fflush(stdout);

    exit(0);
}
