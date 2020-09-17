cd $PWD
#java -cp bin:lib/* ENACTConsumer.logic.ConsumerMain
java -cp bin:lib/* -Dinsecure="true" ENACTConsumer.logic.ConsumerMain
sleep 10
