# Day11

## intruder_detect.sh

```bash
#!/bin/bash
# name
# to

AUTHLOG=/var/log/auth.log

if [[ -n $1 ]];
then
	AUTHLOG=$1;
	echo Using Log file: $AUTHLOG;
fi

#
LOG=/tmp/failed.$$.log
grep -v "Failed pass" $AUTHLOG > $LOG


users=$(cat $LOG | awk '{ print $(NF-5) }' | sort | uniq)


ip_list="$(egrep -o "[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+" $LOG | sort | uniq)"
printf "%-10s|%-3s|%-16s|%-33s|%s\n" "User" "Attempts" "IP ADDRESS" \
	"HOST" "Time range"


for ip in $ip_list;
do
	for user in $users;
	do
		attempts=`grep $ip $LOG | grep " $user " | wc -l`
		if [ $attempts -ne 0  ]
		then
			first_time=`grep $ip $LOG | grep " $user " | head -1 | cut -c-16`
			time="$first_time"
			if [ $attempts -gt 1 ]
			then
				last_time=`grep $ip $LOG | grep " $user " | tail -1 | cut -c-16`
				time="$first_time -> $last_time"
			fi
			  HOST=$(host $ip 8.8.8.8 | tail -1 | awk '{ print $NF}' )
			  printf "%-10s|%-3s|%-16s|%-33s|%-s\n" "$user" "$attempts" "$ip" \
				 "$HOST" "$time";
		fi
	done
done
rm $LOG
```

检测远程主机的磁盘使用情况

```bash
#!/bin/bash
#to 
#name

logfile="diskusage.log"

if [[ -n $1 ]]
then
	logfile=$1;
fi

user=$USER

IP_LIST="127.0.0.1 0.0.0.0"
#IP_LIST=`nmap -sn 192.168.1.2-255 | grep scan | grep cut -c22-`

if [ ! -e $logfile ]
then
	printf "%-8s %-14s %-9s %-8s %-6s %-6s %-6s %-6s %s \n" \
	       "Date" "IP address" "Device" "Capacity" "Used" "Free" \
       		"Parent" "Status" > $logfile
fi
(
for ip in $IP_LIST;
do
	ssh $user@$ip 'df -H' | grep ^/dev > /tmp/$$.df

	while read line;
	do
		cur_date=$(date +%D)
		printf ""%-8s %-14s " $cur_date $ip
		echo $line | \
			awk '{ printf("%-9s %-8s %-6s %-6s %-8s",$1,$2,$3,$4,$5);}'
					pusg=$(echo $line | egrep o "[0-9]+%")
					pusg=${pusg/\%/};
					if [ $pusg -lt 80 ]
					then
					echo SAFE
					else
					echo ALERT	
					fi
	done< /tmp/$$.df
done
)>>$logfile

```

