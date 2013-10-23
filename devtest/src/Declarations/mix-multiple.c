//##################################################################
// Mix of simple declarations (multiple variables declaration)
//##################################################################

//
// Homogeneous Simples declarators
//

// standard types
int i,i1,*i2;
unsigned int ui,ui1,*ui2;
char c,c1, *c2;
unsigned int uc,uc1,*uc2;;
short s, s1, *s2;;
unsigned short us,us1,*us2;
long l,l1,*l2;;
unsigned long ul,ul1,*ul2;;
unsigned int p(int i,char j),* pf2(char),(*pf3(char))(int) ;
int tab[10], tab1[11], *tab12[12];
unsigned long **tab3,***tab31[15];

// extern
extern int ei,ei1,*ei2;
extern unsigned int eui,eui1,*eui2;
extern char ec,ec1,*ec2;
extern unsigned int euc,euc1,*euc2;
extern short es,es1,*es2;
extern unsigned short eus,eus1,*eus2;
extern long el,el1,*el2;
extern unsigned long eul, eul1,*eul2;

// static
static int si,si1,*si2;
static unsigned int sui,sui1,*sui2;
static char sc,sc1,*sc2;
static unsigned int suc,suc1,*suc2;
static short ss,ss1,*ss2;
static unsigned short sus,sus1,*sus2;
static long sl,sl1,*sl2;
static unsigned long sul,sul1,*sul2;;

// volatile
volatile int vi,vi1,*vi2;
volatile unsigned int vui,vui1,*vui2;
volatile char vc,vc1,*vc2;
volatile unsigned int vuc,vuc1,*vuc2;
volatile short vs,vs1,*vs2;
volatile unsigned short vus,vus1,*vus2;
volatile long vl,vl1,*vl2;
volatile unsigned long vul, vul1,*vul2;

// struct
struct S {int i; int j;};
struct {int i; int j;} st1,st11,*st12;
struct S st2,st21,*st22;

// union
union U {int i; int j;};
union UC {int i; struct S j;};
union {int i; int j;} u1,u11,*u12;
union U u2,u21,*u22;

// enum
enum E {A=10, B, C, D};
enum {E=10, F, G, H} e1,e11,*e12;
enum E e2,e21,e22;


// typedef
typedef int ti, ti1, ti2;
typedef unsigned int tui, tui2, tui3;
typedef char tc, tc1, tc2;
typedef unsigned int tuc, tuc1, tuc2;
typedef short ts, ts1, ts2;
typedef unsigned short tus, tus1, tus2;
typedef long tl, tl1, tl2;
typedef unsigned long tul, tul1, tul2;
typedef struct S tS, tS1, tS2;
typedef union U tU, tU1, tU2;
typedef enum E tE, tE1, tE2;
tU a, a1, *a2;
ti b, b1, *b2;

// function prototype
int f1(), f2(int i, char j), *f3(int i, char j);
int (*f4)(int i, char j), (*f5(int i))(int i, char j);

// function
int m(int i) {
  short j, j1, **j2;
  if (i) {
    char c, *c2;
    static int d, *d2;
  }
}
