// Test declaration of variable in for-loop

int f() {
  //  static int j,k;
  struct S {int a; int b;} i;

  // -> for-loop with simple statement
  // Here it the int declaration
  for(int i;i<10;i++) i=i+1;
  
  // Here it's the struct declaration
  i.a=3;

  // -> for-loop with compound
  // Here it the int declaration
  for(int i;i<10;i++) { i=i+1;}

  // Here it's the struct declaration
  i.b=4;

  // Here it the useless 'int' declaration
  for(int ;0;0) {0;}
}
