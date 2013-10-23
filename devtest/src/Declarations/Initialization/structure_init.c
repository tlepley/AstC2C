//
// Initialization of structure
//

// Standard
struct S1 {int a; int b;};
struct S1 s1 = {1,2};

// Mixed
struct S2 {int a; int b;} s2 = {3,4};

// Multidimentionnal
struct S3 {struct S2 s2; struct S1 s1; int i;};
struct S3 s3 = {{1,2},{3,4},5};
