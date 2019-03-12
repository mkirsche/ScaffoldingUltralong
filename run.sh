contigsfn=$1
readsfn=$2
paffn=$3
outfile=$4

minimappath=/scratch/groups/mschatz1/mkirsche/github/minimap2/minimap2

if [ ! -f $paffn ]; then
    echo "PAF file not found - generating it"
    $minimappath -t 8 $contigsfn $readsfn  > $paffn
fi

BINDIR=`dirname $(readlink -f "$0")`

echo 'Compiling'
javac $BINDIR/scaffolding/*.java

usefulpaf=$paffn'_useful.paf'
echo 'Finding useful alignments'
java -cp $BINDIR scaffolding.FindUsefulScaffoldingAlignments $paffn $usefulpaf
echo 'Useful alignments output to '$usefulpaf

readmap=$readsfn'_usefulmap.paf'
contigmap=$contigsfn'_usefulmap.paf'
newcontigs=$contigsfn'_newcontigs.paf'
echo 'Scaffolding'
java -cp $BINDIR scaffolding.Scaffold $usefulpaf $contigsfn $readsfn $readmap $contigmap $newcontigs
echo 'Scaffolds output to '$newcontigs

echo 'Integrating scaffolds into assembly'
java -cp $BINDIR scaffolding.StitchFasta $contigsfn $newcontigs $outfile
echo 'Final assembly output to '$outfile
