//##################################################################
// Variables and types overriding
//##################################################################


int a;
int b;


// Simple overriding
int f1(int a) {	// overriding global
  int b;	// overriding global
  if (a) {	// refer to local
    int a=1;
    return(a+b); // refer to scope 'a' and local 'b'
  }
  return(a); // refer to local
}


//Embedded scopes 
int f2() {
  int a;
  if (b) {
    int a;
    if (b) {
      int a;
      if (b) {
	int a;
	if (b) {
	  int a;
	  if (b) {
	    int a;
	    if (b) {
	      int a;
	      if (b) {
	      }
	      return(a);
	    }
	    return(a);
	  }
	  return(a);
	}
	return(a);
      }
      return(a);
    }
    return(a);
  }
}
