# push raw tx hex
curl http://localhost:5000/bcrt1qkpcc997zxsp3kdkryx4w2unx2etqktwx5x3kv2hyn0d5ra75zrkqn5kh5a -d "{"tx"="", "tx_hex"=""}" -X put -v
 
# get unsigned tx 

curl http://127.0.0.1:5000/api/sign/bcrt1qkpcc997zxsp3kdkryx4w2unx2etqktwx5x3kv2hyn0d5ra75zrkqn5kh5a
{
"hex": "45505446ff00020000000001011b387a149aec22d9ad42bf1d0be633cbc60b26deef1c5f9de68dd8cacae61d350000000000fdffffff0200c2eb0b00000000220020b0718297c234031b36c321aae5726656560b2dc6a1a3662ae49bdb41f7d410ec2600af2f000000002200204f1827f75d67222f84f847be780fb8546ded66af46fbe09479ba53160493c778feffffffff00ca9a3b000000000000050001ff01ff01fffd0201524c53ff02575483011b8a76e680000001579476c6335c800200fa674973156d3a32091491f9250ede6af8be4b364bd38002d07e3f983e9c0d7e6f4241470cdbbd92b846e090f11b3ca89d5fac233cdfd632000000004c53ff0257548301045dc927800000019ce9cac030c62a609e3a87bc870dfbfc2001a4ada3abdc8d4fc0f88a2badea6d02f371717b2d8d5bf6ada0dd75afabd2913b6133f2edea52e4c66a7f7c3846ed94000000004c53ff02575483012f95c27880000001720d79f2ece1327da3af01b1e4cf451b8182f064e185805e76ebed6ffc2fbcdf031cc7014d3c2c7f5aa515269ae7da63a3b1c070974a2dc8c8bba4ea04f6e2aa8b0000000053aefb000000",
"complete": false,
"final": false,
"tx": {
"partial": true,
"version": 2,
"segwit_ser": true,
"inputs": [
{
"prevout_hash": "351de6cacad88de69d5f1cefde260bc6cb33e60b1dbf42add922ec9a147a381b",
"prevout_n": 0,
"scriptSig": "",
"sequence": 4294967293,
"type": "p2wsh",
"address": "bcrt1qkpcc997zxsp3kdkryx4w2unx2etqktwx5x3kv2hyn0d5ra75zrkqn5kh5a",
"num_sig": 2,
"x_pubkeys": [
"ff02575483011b8a76e680000001579476c6335c800200fa674973156d3a32091491f9250ede6af8be4b364bd38002d07e3f983e9c0d7e6f4241470cdbbd92b846e090f11b3ca89d5fac233cdfd63200000000",
"ff0257548301045dc927800000019ce9cac030c62a609e3a87bc870dfbfc2001a4ada3abdc8d4fc0f88a2badea6d02f371717b2d8d5bf6ada0dd75afabd2913b6133f2edea52e4c66a7f7c3846ed9400000000",
"ff02575483012f95c27880000001720d79f2ece1327da3af01b1e4cf451b8182f064e185805e76ebed6ffc2fbcdf031cc7014d3c2c7f5aa515269ae7da63a3b1c070974a2dc8c8bba4ea04f6e2aa8b00000000"
],
"pubkeys": [
"030200de8f67729016888571f81b0ea98bd9dd277fb26007e6766492867bf2b03f",
"0355166383096e4c659acad9785820e512f8dbcd813062177f9e260ff5ffdb8c42",
"03d8426ad2d850f2d40745ad9c6a3f5b118a8b5758f646c1971a0d5a5dbb6cc36a"
],
"signatures": [
null,
null,
null
],
"value": 1000000000,
"witness_version": 0,
"witness": "050001ff01ff01fffd0201524c53ff02575483011b8a76e680000001579476c6335c800200fa674973156d3a32091491f9250ede6af8be4b364bd38002d07e3f983e9c0d7e6f4241470cdbbd92b846e090f11b3ca89d5fac233cdfd632000000004c53ff0257548301045dc927800000019ce9cac030c62a609e3a87bc870dfbfc2001a4ada3abdc8d4fc0f88a2badea6d02f371717b2d8d5bf6ada0dd75afabd2913b6133f2edea52e4c66a7f7c3846ed94000000004c53ff02575483012f95c27880000001720d79f2ece1327da3af01b1e4cf451b8182f064e185805e76ebed6ffc2fbcdf031cc7014d3c2c7f5aa515269ae7da63a3b1c070974a2dc8c8bba4ea04f6e2aa8b0000000053ae",
"witness_script": "5221030200de8f67729016888571f81b0ea98bd9dd277fb26007e6766492867bf2b03f210355166383096e4c659acad9785820e512f8dbcd813062177f9e260ff5ffdb8c422103d8426ad2d850f2d40745ad9c6a3f5b118a8b5758f646c1971a0d5a5dbb6cc36a53ae"
}
],
"outputs": [
{
"value": 200000000,
"type": 0,
"address": "bcrt1qkpcc997zxsp3kdkryx4w2unx2etqktwx5x3kv2hyn0d5ra75zrkqn5kh5a",
"scriptPubKey": "0020b0718297c234031b36c321aae5726656560b2dc6a1a3662ae49bdb41f7d410ec",
"prevout_n": 0
},
{
"value": 799997990,
"type": 0,
"address": "bcrt1qfuvz0a6avu3zlp8cg7l8srac23k76e40gma7p9rehff3vpyncauqlzhqfa",
"scriptPubKey": "00204f1827f75d67222f84f847be780fb8546ded66af46fbe09479ba53160493c778",
"prevout_n": 1
}
],
"lockTime": 251
},
"rid": 2
}

# post signed tx hex

curl http://127.0.0.1:5000/api/sign/bcrt1qkpcc997zxsp3kdkryx4w2unx2etqktwx5x3kv2hyn0d5ra75zrkqn5kh5a -d "{"tx_hex"="xx", rid="1"}" -X put -v