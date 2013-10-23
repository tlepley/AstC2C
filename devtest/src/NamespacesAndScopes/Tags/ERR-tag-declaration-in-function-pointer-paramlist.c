//#######################################################################
// Tag declaration inside parameters list of a pointer to function
//#######################################################################

// Pointer to function (scope limited to the parameter list of the prototype)
void (*pf)(
	   struct A {
	     union B {
	       int i;
	       char j;
	     } i;
	   } i,
	   enum E {A,B,C=0} j
	   );


// Wrong references
struct A a;
struct B b;
enum E c;
int d=A;
