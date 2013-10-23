int f1();
int f2();
int f3();

typedef int (*toto)();

toto titi[3]={f1,f2,f3};
