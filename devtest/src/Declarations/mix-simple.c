//##################################################################
// Mix of simple declarations
//##################################################################

// standard types
int i;
unsigned int ui;
char c;
unsigned int uc;
short s;
unsigned short us;
long l;
unsigned long ul;
int *p=(int *)0;
int tab[10];
int *tab2[20];
int **tab3;

// extern
extern int ei;
extern unsigned int eui;
extern char ec;
extern unsigned int euc;
extern short es;
extern unsigned short eus;
extern long el;
extern unsigned long eul;
extern int * ep;

// static
static int si;
static unsigned int sui;
static char sc;
static unsigned int suc;
static short ss;
static unsigned short sus;
static long sl;
static unsigned long sul;
static int sp;

// volatile
volatile int vi;
volatile unsigned int vui;
volatile char vc;
volatile unsigned int vuc;
volatile short vs;
volatile unsigned short vus;
volatile long vl;
volatile unsigned long vul;
volatile int vp;

// struct
struct S {int i; int j;};
struct {int i; int j;} s1;
struct S s2;

// union
union U {int i; char j;};
union {int i; int j;} u1;
union U u2;

// enum
enum E {A=10, B, C, D};
enum {E=10, F, G, H} e1;
enum E e2;

// typedef
typedef int ti;
typedef unsigned int tui;
typedef char tc;
typedef unsigned int tuc;
typedef short ts;
typedef unsigned short tus;
typedef long tl;
typedef unsigned long tul;
typedef struct S tS;
typedef union U tU;
typedef enum E tE;
typedef int *tp;
tU a;
ti b;

// function prototypes
void f_proto1();
int f_proto2(int, char);
int f_proto3(int param);
int f_proto4(int,...);
int *f_proto5(int i, char j);
int (*f_proto6(int i, char j))(int,int);

// Pointer to function
int (*fp)(int, char);


// Function definitions
f1() {;}

int f2(int param1, int param2) {
  int local;
}

void (*f3(int i))(int, char) {
  static int s1;
  if (i) {
    char c;
  }
}
