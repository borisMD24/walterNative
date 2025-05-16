public class ContextMenuContext {
    public int nthOppened = -1;
    ContextMenuContext(){

    }
    public void oppened(int nth /*onclose callback as a lambda*/){
        this.nthOppened = nth;
        //set on close lambda
    }
    public void close(int nth){
        if(this.nthOppened == nth){
            this.nthOppened = -1;
            //run on close lambda
            //remove on close lambda
        }
    }
}
