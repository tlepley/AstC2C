//
// Test uncomplete type tag declaration
//

typedef struct _IO_FILE FILE;

typedef struct _IO_FILE __FILE;

struct _IO_FILE;

struct _IO_FILE {
  int _flags;
  char* _IO_read_ptr;
  char* _IO_buf_end;
  struct _IO_FILE *_chain;
};

struct _IO_FILE *_chain;

typedef struct _IO_FILE _IO_FILE;

extern int __underflow (_IO_FILE *) ;

extern int fclose (FILE *__stream);

FILE *p_stat;
