// 'b' is considered as a int by gcc
// the C2C does not see the declaration of 'b' in the parameter list
// It considers that 'b' is a part of an K&R parameter declaration,
// which is not the case here

int g(b)  {
  int a=b;
}

//-------------------------------------------------------------
// Error got with the C2C:
//:7: error :  symbol 'b' not defined
//stopping the compilation process...
//-------------------------------------------------------------
