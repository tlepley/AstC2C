//##################################################################
// Multiple embedded union declaration
//##################################################################


union U1 {	   // global scope
  union U2 {	   // global scope
    char c;
  } i;
  union U3 {	   // global scope
    union U4 {	   // global scope
      union U5 {  // global scope
	char c;
	int i;
      } i;
      char c;
    } i;
    char j;
  } j;
  int k;
};


// References
union U1 u1;
union U2 u2;
union U3 u3;
union U4 u4;
union U4 u5;


