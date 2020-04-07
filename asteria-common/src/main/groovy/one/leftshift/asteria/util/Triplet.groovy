package one.leftshift.asteria.util

import groovy.transform.EqualsAndHashCode
import groovy.transform.TypeChecked

@TypeChecked
@EqualsAndHashCode
class Triplet<L, M, R> {

    final L left
    final M middle
    final R right

    private Triplet(L left, M middle, R right) {
        this.left = left
        this.middle = middle
        this.right = right
    }

    static <L,M,R> Triplet<L, M, R> of(L left, M middle, R right) {
        Assert.notNull(left as Object, middle as Object, right as Object)

        return new Triplet(left, middle, right)
    }
}
