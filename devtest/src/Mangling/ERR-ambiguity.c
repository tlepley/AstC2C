#pragma ast_mangling on
void f(int i, unsigned int j);
void f(float i, unsigned int j);
#pragma ast_mangling off


void main() {
  // Both prototypes require 2 conversions, so that there is an ambiguity
  f((char)1,2);
}
