//##################################################################
// Multiple embedded struct declaration
//##################################################################


struct S1 {	   // global scope
  struct S2 {	   // global scope
    char c;
  } i;
  struct S3 {	   // global scope
    struct S4 {	   // global scope
      struct S5 { // global scope
	char c;
      } i;
      char c;
    } i;
    char j;
  } j;
  int k;
};


// References
struct S1 s1;
struct S2 s2;
struct S3 s3;
struct S4 s4;
struct S4 s5;
