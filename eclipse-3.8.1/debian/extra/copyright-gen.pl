#!/usr/bin/perl

use strict;
use warnings;

my $licpatterns = {
    'LGPL-2.1' => qr/GNU Lesser General Public License \(LGPL\) Version 2.1/o,
    'EPL-1.0' => qr/Eclipse Public License v1.0/o,
};
my $assumed = "EPL-1.0";
my $dcp = 'IBM\s+Corporation\s*,?\s*and\s+others?';
my $curFile = '';

my $lic = {$assumed => {'IBM and others.' => ["2000-2010"]},
           "Apache (v2.0)" => {"Apache Software Foundation" => [[], ["eclipse/plugins/org.apache.ant_1.7.1.v20090120-1145/bin/antrun.p[ly]",
								     "eclipse/plugins/org.apache.ant_1.7.1.v20090120-1145/bin/antRun.pl", 
								     "eclipse/plugins/org.apache.ant_1.7.1.v20090120-1145/bin/complete-ant-cmd.pl",
								     "eclipse/plugins/org.eclipse.equinox.ds/src/org/apache/*"]]} };
my $lf = {
    'eclipse/plugins/org.apache.ant_1.7.1.v20090120-1145/bin' => [4, "Apache (v2.0)"],
    'eclipse/plugins/org.eclipse.equinox.ds/src/org/apache' => [1, "Apache (v2.0)"],
};
my $slash = '/';
my $at = '@';

open(CP, "licensecheck -r --copyright * | ") or choke("Cannot run licensecheck: $!");

while(my $line = <CP>){
    my ($file, $license);
    chomp($line);
    next if($line eq '');
    if($line =~ m/UNKNOWN/o){
        ($file, undef) = split(/:\s*+/o, $line, 2);
        $license = read_license($file);
        if(!defined($license)){
            print STDERR "I: Cannot find the license of $file, assuming $assumed\n" unless($file =~ m@^debian/@o or $file =~ m/templates/o);
            $license = $assumed;
        }
    } else {
        ($file, $license) = split(/:\s*+/o, $line);
        $license =~ s/\s+$//o;
    }

    if($line !~ m/\*No copyright\*/){
	my $cpline = <CP>;
	my @cps = ();

	next if($license =~ m/GENERATED FILE/o);
	next if($file =~ m@^debian/@o or $file =~ m@eclipse/plugins/org.eclipse.jdt.apt.core/src/com/sun/mirror/@o);
	if($file =~m@eclipse/plugins/org.eclipse.swt/Eclipse SWT Mozilla/common/library/@){
	    #print "W: Handle $file manually\n";
	    next;
	}
	chomp($cpline);
	$cpline =~ s/[^:]++:\s*+//o;
	$cpline =~ s@^[^/]*holder[^/]*/@@o;
	$cpline =~ s/All rights?\s*reser.*$//io;
	$cpline =~ s/(\s*\.)?(\s*.?)?$//o;
	$cpline =~ s@\{\s*/\s*has\s*/\s*@@o;
        # Seriously - this should not be necessary!

	if($cpline =~ m@\d{2}[^/]+/\s*\d{2}@) {
	    my @holders = split(/\s*\/\s*/o, $cpline);
	    my $rcp = '';
	    foreach my $holder (@holders){
		my ($hcp, $hyears) = handleCP($holder);
		if($hyears eq '?'){
		    print STDERR "W: Handle $file manually - cannot determine year - $holder ($hcp).\n";
		    @cps = ();
		    last;
		}
		if($hcp =~m/^\s*$/o){
		    print STDERR "W: Handle $file manually - cannot determine copyright holder.\n";
		    last;
		}
		push(@cps, [$hcp, $hyears]);
	    }
	} else {
	    my $y;
	    ($cpline, $y) = handleCP($cpline);
	    push(@cps, [$cpline, $y]);
	    if($y eq '?'){
		print STDERR "W: Handle $file manually - cannot determine year - $cpline.\n";
		next;
	    }
	    if($cpline =~m/^\s*$/o){
		print STDERR "W: Handle $file manually - cannot determine copyright holder.\n";
		next;
	    }
	}
	foreach my $val (@cps){
	    my $cp = $val->[0];
	    my $years = $val->[1];
	    my $folder = $file;
	    my $fl;
	    $folder =~ s@/[^/]+$@@o;
	    $fl = $lf->{$folder};
	    if(!defined($fl)){
		$fl = [1, $license];
		$lf->{$folder} = $fl;
	    } else {
		if($fl->[1] eq $license && $fl->[0] != -1){
		    $fl->[0] = $fl->[0] + 1;
		} else {
		    $fl->[0] = -1;
		}
	    }
	    next if($license eq $assumed and ($cp =~ m/$dcp/o or $cp =~ m/IBM\s*,?\s*and\s+other/ ));
	    if($license eq $assumed){
		my $hmap = $lic->{$license};
		my $ylist;
		if(!defined($hmap)){
		    $hmap = {};
		    $lic->{$license} = $hmap;
		    $ylist = [];
		    $hmap->{$cp} = $ylist;
		} else {
		    $ylist = $hmap->{$cp};
		    if(!defined($ylist)){
			$ylist = [];
			$hmap->{$cp} = $ylist;
		    }
		}
		push(@$ylist, $years);
		next;
	    }
	    my $hmap = $lic->{$license};
	    my $hdata = undef;
	    my $ylist;
	    my $flist;
	    if(!defined($hmap)){
		$hmap = {};
		$lic->{$license} = $hmap;
	    }
	    $hdata = $hmap->{$cp};
	    if(!defined($hdata)){
		$hdata = [[], []];
		$hmap->{$cp} = $hdata;
	    }
	    $ylist = $hdata->[0];
	    $flist = $hdata->[1];
	    push(@$ylist, $years);
	    push(@$flist, $file);
	}
    }
}

my $eplcp = mergeHolders($lic->{$assumed});
delete($lic->{$assumed});
print "Files: *\nCopyright: " . join("\n           ", @$eplcp) . "\nLicense: $assumed\n\n";

while ( my ($license, $holderMap) = each(%$lic)){
    my $files = [];
    my $cr = [];
    my $li = $license;
    if($license =~ m/(\S+)\s*\([vV]([^\)]+)\)/o){
	$li = "$1-$2";
    }
    while( my ($holder, $hdata) = each(%$holderMap) ){
	my ($ylist, $flist) = @$hdata;
	my $crdata = parseYears($ylist) . ", $holder";
	push(@$cr, $crdata);
	generateFileList($files, $flist);
    }
    print "Files: ". join("\n       ", unique(sort(@$files))) . "\n";
    print "Copyright: " .  join("\n           ", @$cr) . "\nLicense: $li\n\n";
}

# hardcoded files.

print <<EOF
Files: eclipse/plugins/org.eclipse.ui.ide/src/org/eclipse/ui/internal/views/markers/MarkerSortUtil.java
Copyright: 1994, Hewlett-Packard Company
           1997, Silicon Graphics Computer Systems, Inc
License: other
 Permission to use, copy, modify, distribute and sell this software
 and its documentation for any purpose is hereby granted without fee,
 provided that the above copyright notice appear in all copies and
 that both that copyright notice and this permission notice appear
 in supporting documentation.  Hewlett-Packard Company makes no
 representations about the suitability of this software for any
 purpose.  It is provided "as is" without express or implied warranty.
 .
 Permission to use, copy, modify, distribute and sell this software
 and its documentation for any purpose is hereby granted without fee,
 provided that the above copyright notice appear in all copies and
 that both that copyright notice and this permission notice appear
 in supporting documentation.  Silicon Graphics makes no
 representations about the suitability of this software for any
 purpose.  It is provided "as is" without express or implied warranty.

Files: eclipse/plugins/org.eclipse.jdt.apt.core/src/com/sun/mirror/*
Copyright: 2004, Sun Microsystems, Inc
License: other
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 .
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and${slash}or other materials provided with the distribution.
     * Neither the name of the Sun Microsystems, Inc. nor the names of
       its contributors may be used to endorse or promote products derived from
       this software without specific prior written permission.
 .
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Files: debian/*
Copyright: 2010-2011, Debian Orbital Alignment Team <pkg-java-maintainers${at}lists.alioth.debian.org>
License: EPL-1.0

EOF
    ;

close(CP);

exit(0);

sub read_license{
    my $file = shift;
    my $i = 0;
    my $limit = 15;
    my $l = undef;
    open(my $fd,  "<", $file) or die("$file: $!");
    READ: while( my $line = <$fd> ){
        chomp($line);
        while ( my ($lic, $pat) = each(%$licpatterns) ){
            if($line =~ $pat){
                $l = $lic;
                last READ;
            }
        }
        $i++;
        last READ if($i >= $limit)
    }
    close($fd);
    return $l;
}

sub choke{
    my $msg = shift;
    print STDERR "$msg\n";
    exit(1);
}

sub generateFileList {
    my $allFiles = shift;
    my $toAdd = shift;
    foreach my $file (@$toAdd){
	my $dir = $file;
	my $dirdata;
	$dir =~ s@/[^/]++$@@o;
	$dirdata = $lf->{$dir};
	if($dirdata->[0] == 0){
	    next;
	}
	if($dirdata->[0] > 0){
	    # Use wildcard.
	    $file = "$dir/*";
	    $dirdata->[0] = 0;
	}
	if($file =~ m/[ \t]/o){
	    $file = "\"$file\"";
	}
	push(@$allFiles, $file);
    }
    return 1;
}

sub handleCP{
    my $cp = shift;
    my $years = $cp;
    if($cp =~ m/^\s*+(\d+(?:\s*+[,-]\s*+\d+)*)/o){
	$years = $1;
	$cp =~ s/^\s*+\d+(?:\s*+[,-]\s*+\d+)*\s*+//;
    } else {
	if($cp =~ m/\((\d+(?:\s*+[,-]\s*+\d+)*)\)/o){
	    $years = $1;
	    $cp =~ s/\((\d+(?:\s*+[,-]\s*+\d+)*)\)//o;
	} else {
	    $years = '?';
	}
    }
    $cp =~ s/^s?\s+//o;
    $cp =~ s/\bCorp\.?\b/Corporation/oi;
    $cp =~ s@/\s*\|\|.+$@@o;
    $cp =~ s/^\s*+(,|by|the)\s*+//oi;
    $cp =~ s/\s*+$//o;
    return ($cp, $years);
}

sub parseYears{
    my $years = shift;
    my $min = -1;
    my $max = -1;
    foreach my $line (@$years){
	foreach my $e (split(/\s*,\s*/o, $line)){
	    if($e =~ m/(\d+)[ \t]*-[ \t]*(\d+)/o){
		my $m1 = $1;
		my $m2 = $2;
		if($m2 < $1){
		    my $tmp = $m2;
		    $m2 = $m1;
		    $m1 = $tmp;
		}
		$min = $m1 if($min > $m1 or $min == -1);
		$max = $m2 if($m2 > $max or $max == -1);
	    } else {
		$min = $e if($min > $e or $min == -1);
		$max = $e if($e > $max or $max == -1);
	    }
	}
    }
    if($min == -1 or $max == -1){
	choke("Cannot determine years " . join(" - ", @$years));
    }
    if($min == $max){
	return $min;
    }
    return "$min-$max";
}

sub mergeHolders{
    my $hmap = shift;
    my $ret = [];
    foreach my $holder (sort(keys(%$hmap))) {
	my $ylist = $hmap->{$holder};
	my $y = parseYears($ylist);
	push(@$ret, "$y, $holder");
    }
    return $ret;
}

sub unique {
    my @unique = ();
    my %seen   = ();
    foreach my $elem ( @_ ) {
	next if $seen{ $elem }++;
	push @unique, $elem;
    }
    return @unique;
}
