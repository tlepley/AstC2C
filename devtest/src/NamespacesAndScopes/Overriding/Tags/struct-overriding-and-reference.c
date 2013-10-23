//##################################################################
// Struct type tag overriding
//##################################################################


struct S1 {int i;};


// Overriding by complete definition
struct S1 f1_struct() {      // refers to global
  struct S1 s1; 	      // refers to global
  struct S1 {int i; int j;}; // defines local
  struct S1 s2;	   	      // refers to local
  return(s1);
}
// Overriding by incomplete definition
void f2_struct() {
  struct S1;		      // defines incomplete local
  struct S1 *s1; 	      // refers to local
  struct S1 {int i; int j;}; // completes incomplete definition
  struct S1 s2;	   	      // refers to local
  *s1=s2;
}
void f3_struct(int i) {
  struct S1 s1; 	      // refers to global
  struct S1 {int i; int j;}; // defines local
  struct S1 s2; 	      // refers to local
  if (i) {
    struct S1 {int i; int j; int k;}; // defines scope local
    struct S1 s3;		       // refers to scope
  }
  {
    struct S1 s4;      // refer to local
    s4=s2;		// *compatible*
  }
}
