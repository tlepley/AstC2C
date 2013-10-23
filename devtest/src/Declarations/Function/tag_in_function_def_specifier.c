//##################################################################
// Type tag declaration in the function definition specifier
//##################################################################


// Complete definition
//--------------------

// Struct
struct S1 {int i; int j;}* f1() {
  ;
}
struct S1 s1;


// Union
union U1 {int i; char j;} f2() {
  ;
}
union U1 u1;


// Enum
enum E1 {A, B=0} f3() {
  ;
}
enum E1 e1 = B;





// Incomplete definition
//----------------------

// Struct
struct S2 *f4() {
  ;
}
struct S2 {int i; int j;} s2;


// Union
union U2 *f5() {
  ;
}
union U2 {int i; char j;} u2;


// Enum
enum E2 *f6() {
  ;
}
enum E2 {C=0, D} e2 = D;




