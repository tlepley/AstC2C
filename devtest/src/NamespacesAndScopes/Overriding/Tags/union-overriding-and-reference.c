//##################################################################
// Union type tag overriding
//##################################################################


union U1 {int i;};


// Overriding by complete definition
union U1 f1_union() {
  union U1 u1; 	      // refer to global
  union U1 {int i; int j;}; // define local
  union U1 u2;	   	      // refer to local
  return(u1);
}
// Overriding by incomplete definition
void f2_union() {
  union U1;		      // define incomplete local
  union U1 *u1; 	      // refer to local
  union U1 {int i; int j;}; // complete incomplete definition
  union U1 u2;	   	      // refer to local
  *u1=u2;
}
void f3_union(int i) {
  union U1 u1; 	      // refer to global
  union U1 {int i; int j;}; // define local
  union U1 u2; 	      // refer to local
  if (i) {
    union U1 {int i; int j; int k;}; // define scope local
    union U1 u3;		       // refer to scope
  }
  {
    union U1 u4;      // refer to local
    u4=u2;		// *compatible*
  }
}
