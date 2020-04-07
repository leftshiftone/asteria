package one.leftshift.asteria.util

import groovy.transform.EqualsAndHashCode
import groovy.transform.TypeChecked

@TypeChecked
@EqualsAndHashCode
class Tuple<L,R> {
    final L left
    final R right

    private Tuple(L left, R right) {
        this.left = left
        this.right = right
    }

    static <L,R> Tuple<L, R> of(L left, R right) {
        Assert.notNull(left as Object, right as Object)

        return new Tuple(left, right)
    }
}
