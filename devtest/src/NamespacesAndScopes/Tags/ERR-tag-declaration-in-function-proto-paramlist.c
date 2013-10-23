//#######################################################################
// Tag declaration inside the parameter list of a prototype
//#######################################################################


// Scope limited to the parameter list of the prototype
void proto_f(
	     struct A {union B {int i; char j;} i; int j;} i,
	     enum E {A,B,C=0} j
	     );


// Wrong references
struct A a;
struct B b;
enum E c;
int d=A;

