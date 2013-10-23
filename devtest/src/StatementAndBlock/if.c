// C99, 6.8.4.1 : the controling expression of an if shall have the scalar type

void f() {
  char c;
  unsigned char uc;
  short s;
  unsigned short us;
  int i;
  unsigned int ui;
  long l;
  unsigned long ul;
  float f;
  
  void *p;
  int  T[10];

  if (c) {c=1;}
  if (uc) {uc=1;}
  if (s) {s=1;}
  if (us) {us=1;}
  if (i) {i=1;}
  if (ui) {ui=1;}
  if (l) {l=1;}
  if (ul) {ul=1;}
  if (f) {f=1.0;}
  if (p) {p=&i;}
  if (T) {T[0]=1;}
}
