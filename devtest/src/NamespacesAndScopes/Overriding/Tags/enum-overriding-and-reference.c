//##################################################################
// Enum type tag overriding
//##################################################################


enum E1 {A=0, B};

// Overriding by complete definition
enum E1 f1_enum() {
  enum E1 e1 = B;     	// refer to global
  enum E1 {A=4, B};	// define local
  enum E1 e2;		// refer to local 'E1'
  //printf("f1[%d]", e1);
  e1=B;e2=B;		// refer to local 'B' in both cases
  //printf("f1[%d %d]", e1, e2);
  return(e1);
}
// Overriding by incomplete definition
void f2_enum() {
  enum E1;		// define incomplete local
  enum E1 *e1;		// refer to local
  enum E1 {A=4, B};	// complete incomplete definition
  enum E1 e2;		// refer to local
  enum E1 e3;		// refer to local
  e1=&e3;*e1=B;e2=B;	// refer to local 'B' in both cases
  //printf("f2[%d %d]", *e1, e2);
}
void f3_enum() {
  enum E1 e1=B;		// refer to global
  enum E1 {A=4, B};	// define local
  enum E1 e2;		// refer to local
  e2=B;
  //printf("f3[%d %d]", e1, e2);

  if (1) {
    enum E1 {A=9, B, C};	// define scope local
    enum E1 e3;			// refer to scope
    e3=B;
    //printf("f3[%d]", e3);
  }
  {
    enum E1 e4;      // refer to local
    e4=B;
    //printf("f3[%d]", e4);
  }
}


void main() {
  f1_enum();
  f2_enum();
  f3_enum();
}
