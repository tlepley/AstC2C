//##################################################################
// Parameters in function prototype/definition
//##################################################################


// Function prototypes
//---------------------

int proto_f1(int p1, int p2, int p3);

// Pointer to function
int (*pointer_f)(int p1);

// Function returning a pointer to function
int (*proto_f2(int i, int j))(int p1);



// Function definitions
//----------------------

// Standard function 
int f1(char i, char j) {
  return(i+j);
}

// Function returning a pointer to function
int (*f2(int i, int j))(char p1, char p2) {
  return(f1);
}


