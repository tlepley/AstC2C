//##################################################################
// Test multiple heterogeneous declarations (tag, variable,
//                                           function pointers)
//##################################################################


static struct S1 {int i; int j; } s1,*ps1, * (*f1)(int i, int j);

extern union U1 {int i; char j; } u1,*pu1, * (*f2)(int i, int j);

const struct S2 {int i; int j; } s2,*ps2, * (*f3)(int i, int j);

volatile union U2 {int i; char j; } u2,*pu2, * (*f4)(int i, int j);

