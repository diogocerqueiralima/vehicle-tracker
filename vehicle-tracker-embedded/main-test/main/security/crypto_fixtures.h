#ifndef CRYPTO_FIXTURES_H

#define CRYPTO_FIXTURES_H

#include <stdint.h>

/*
 * Fixtures for the crypto tests.
 *
 * KEY_A_RAW and KEY_B_RAW are the raw private scalars of two different ECDSA P-256 keys.
 * The certificates are self signed by key A, with the subject CN=test-device:
 * - CERT_A_VALID is valid from 2020 to 2999;
 * - CERT_A_EXPIRED was valid from 2000 to 2001;
 * - CERT_A_FUTURE is valid from 2900 to 2999;
 * - CERT_RSA_VALID is self signed by an RSA 2048 key, valid from 2020 to 2999;
 * - CERT_CA_VALID is a self signed CA certificate (CN=test-ca, basic constraints CA:TRUE), valid from 2020 to 2999.
 */

static const uint8_t KEY_A_RAW[] = {
    0xc6, 0xfd, 0x01, 0x7f, 0xc7, 0x01, 0x74, 0x6a, 0x7e, 0x2d,
    0x8f, 0xf5, 0x85, 0xa0, 0x56, 0xc1, 0x27, 0x78, 0xd3, 0xaf,
    0x7a, 0x38, 0xcf, 0x1b, 0xa3, 0xb2, 0x16, 0xf6, 0xb0, 0xaf,
    0xc5, 0x7f
};
static const uint8_t KEY_B_RAW[] = {
    0x49, 0xe7, 0x1b, 0x61, 0xc8, 0x1c, 0xf2, 0x76, 0x97, 0x8e,
    0x82, 0x01, 0x5c, 0xd4, 0xb2, 0xf0, 0x95, 0x00, 0xed, 0x25,
    0x2b, 0xad, 0xbe, 0xcf, 0x37, 0x07, 0x87, 0xb8, 0xd5, 0x22,
    0x7e, 0xf4
};

static const char CERT_A_VALID[] =
    "-----BEGIN CERTIFICATE-----\n"
    "MIIBGjCBwaADAgECAgEBMAoGCCqGSM49BAMCMBYxFDASBgNVBAMMC3Rlc3QtZGV2\n"
    "aWNlMCAXDTIwMDEwMTAwMDAwMFoYDzI5OTkwMTAxMDAwMDAwWjAWMRQwEgYDVQQD\n"
    "DAt0ZXN0LWRldmljZTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABP+he6Asz5Jt\n"
    "qtDkHkkZ++XrVQ3ddDGY37iyaQh9kO88ijcsWg9RSbAdRLZ4rQBnPhiy05lYipMd\n"
    "DzqmwJ3CcM4wCgYIKoZIzj0EAwIDSAAwRQIhAPTnCnTHYKqJq/dlgTyGnJb4htfF\n"
    "J4pF3VsYNj1zrOBGAiB56dUx+Vj9fPbZSgld5VT7zbuCerLgJloAYfSTx/YTug==\n"
    "-----END CERTIFICATE-----\n";

static const char CERT_A_EXPIRED[] =
    "-----BEGIN CERTIFICATE-----\n"
    "MIIBGTCBv6ADAgECAgEBMAoGCCqGSM49BAMCMBYxFDASBgNVBAMMC3Rlc3QtZGV2\n"
    "aWNlMB4XDTAwMDEwMTAwMDAwMFoXDTAxMDEwMTAwMDAwMFowFjEUMBIGA1UEAwwL\n"
    "dGVzdC1kZXZpY2UwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAT/oXugLM+SbarQ\n"
    "5B5JGfvl61UN3XQxmN+4smkIfZDvPIo3LFoPUUmwHUS2eK0AZz4YstOZWIqTHQ86\n"
    "psCdwnDOMAoGCCqGSM49BAMCA0kAMEYCIQDDZImTat2zqX+vc0qrbaNU76hSzCE5\n"
    "fj5qrz0QbMXxqAIhALIaF87P4hAj7EntpVulmHiH5xe2kNZnxYetfBIt4A3X\n"
    "-----END CERTIFICATE-----\n";

static const char CERT_A_FUTURE[] =
    "-----BEGIN CERTIFICATE-----\n"
    "MIIBHDCBw6ADAgECAgEBMAoGCCqGSM49BAMCMBYxFDASBgNVBAMMC3Rlc3QtZGV2\n"
    "aWNlMCIYDzI5MDAwMTAxMDAwMDAwWhgPMjk5OTAxMDEwMDAwMDBaMBYxFDASBgNV\n"
    "BAMMC3Rlc3QtZGV2aWNlMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/6F7oCzP\n"
    "km2q0OQeSRn75etVDd10MZjfuLJpCH2Q7zyKNyxaD1FJsB1EtnitAGc+GLLTmViK\n"
    "kx0POqbAncJwzjAKBggqhkjOPQQDAgNIADBFAiEAm2D+mEkFa8teYxjVMhHlmwbh\n"
    "kJ3OI520mh5SR7XjUSgCIAu303HuRFXMl0hjUQtb4/pxUdqWnbQL4utZkw7zKBjl\n"
    "-----END CERTIFICATE-----\n";

static const char CERT_RSA_VALID[] =
    "-----BEGIN CERTIFICATE-----\n"
    "MIICpzCCAY+gAwIBAgIBATANBgkqhkiG9w0BAQsFADAWMRQwEgYDVQQDDAt0ZXN0\n"
    "LWRldmljZTAgFw0yMDAxMDEwMDAwMDBaGA8yOTk5MDEwMTAwMDAwMFowFjEUMBIG\n"
    "A1UEAwwLdGVzdC1kZXZpY2UwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIB\n"
    "AQD5MNXT9chkDcBmQZ7xsHzWv8p98RCiVUBrJrxfVhgm6h7owgVy5fMp/lph5srT\n"
    "GYfL7j86F1mIS5EXLjCFAr52djV51PXFSu73flx+UGvIK7MTQlkygYM2QeXqZ2J8\n"
    "8JCwFBmubVC44D52ElVlom4NeXuHKsmC/d+kNffziu8cHq0c/BENCQe4wdjgOwIc\n"
    "7oe+EadZ5bDwt5bVQ2udBuhke7fLzErq/Q0enlxqAc+/gii5ptf6wJ9q1u3GJM0U\n"
    "eGcrwhK0WiD5fIyUow2bhNXbBWJARZvRtnAVEteWNvv9iKZ+UkR1cDn6uFTD5Be7\n"
    "KTXvI4pENvOu/29CpGgUFI+LAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAD4a5snk\n"
    "wnBILOLxi5wOo9m/y5mqwYyr3J50FefxdhAKaeGQDBIgfmbdMgPdu0y1zHaAGsve\n"
    "MJW934n5XusCPErozY8FyX0kReiEiCGC8S3vjTmbGN4oKvZnI3OO/Ib2AVyAy6jt\n"
    "JDt/eB4p+6aJ8qZFJSpTBaWUsDcUhxDo/nwXb8iC9UnPgWgJ0UMv2vFv2ids3qgt\n"
    "/r3OoAZxKD7FVNVWY51WDcJLBuRfkW13keySOc+SOnH+2Nr2B2Pz2vUKr6Zf9Qn+\n"
    "P9bgJlXAZCXHOZcolr7y58eGF6C939mQU2dUCk6LchDd0CuNgTv1TPuFyEOBVydK\n"
    "pRQLU/f7BkhMO9Y=\n"
    "-----END CERTIFICATE-----\n";

static const char CERT_CA_VALID[] =
    "-----BEGIN CERTIFICATE-----\n"
    "MIIBKDCBzqADAgECAgEBMAoGCCqGSM49BAMCMBIxEDAOBgNVBAMMB3Rlc3QtY2Ew\n"
    "IBcNMjAwMTAxMDAwMDAwWhgPMjk5OTAxMDEwMDAwMDBaMBIxEDAOBgNVBAMMB3Rl\n"
    "c3QtY2EwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQL7lK76XY842cVBKpiHnl6\n"
    "1Zdm5CaNtg31A269+gEoXkCaHo41KDtPYCOU8ioAVpS+zbRJIfFMCf1f7TBos5eo\n"
    "oxMwETAPBgNVHRMBAf8EBTADAQH/MAoGCCqGSM49BAMCA0kAMEYCIQCoZViI97Yq\n"
    "1lOsRB8Nw6Qs/zOywOc54kFjAzix7JIFXwIhAL2Rvp5B7jyc0Y2goXz5c6fKFmJM\n"
    "ew+uhstKfHcm1Lce\n"
    "-----END CERTIFICATE-----\n";

#endif
