package scaffolding;

/*
Scaffolding assemblies with long-read alignments
 */
import java.util.*;
import java.io.*;
public class FindUsefulScaffoldingAlignments {
@SuppressWarnings("resource")
public static void  main(String[] args) throws IOException
{
	String pafFn = "rel2_200kplus_ccs.paf";
	String outFn = "rel2_200kplus_ccs_useful.paf";
	
	if(args.length > 0 && args[0].equals("--server"))
	{
		pafFn = "/scratch/groups/mschatz1/mkirsche/ultralong/ccs/rel2_200kplus_ccs.paf";
		outFn = "/scratch/groups/mschatz1/mkirsche/ultralong/ccs/rel2_200kplus_ccs_useful.paf";
	}
	
	else if(args.length >= 2)
	{
		pafFn = args[0];
		outFn = args[1];
	}
	
	Scanner input = new Scanner(new FileInputStream(new File(pafFn)));
	PrintWriter out = new PrintWriter(new File(outFn));
	HashMap<String, ArrayList<PafAlignment>> alignmentsPerRead = new HashMap<>();
	while(input.hasNext())
	{
		String line = input.nextLine();
		PafAlignment cur = new PafAlignment(line);
		String readName = cur.readName;
		if(!alignmentsPerRead.containsKey(readName)) alignmentsPerRead.put(readName, new ArrayList<PafAlignment>());
		alignmentsPerRead.get(readName).add(cur);
	}
	int tot = 0;
	for(String s : alignmentsPerRead.keySet()) 
		if(alignmentsPerRead.get(s).size() == 2)
		{
			PafAlignment first = alignmentsPerRead.get(s).get(0);
			PafAlignment second = alignmentsPerRead.get(s).get(1);
			if(first.readStart > second.readStart)
			{
				PafAlignment tmp = first;
				first = second;
				second = tmp;
			}
			if(first.contigName.equals(second.contigName))
			{
				continue;
			}
			if(Math.abs(first.readEnd - first.readStart) < 10000 || Math.abs(second.readEnd - second.readStart) < 10000)
			{
				continue;
			}
			
			int maxHanging = 100;
			if(first.contigStart > maxHanging && first.contigEnd < first.contigLength - maxHanging)
			{
				continue;
			}
			if(second.contigStart > maxHanging && second.contigEnd < second.contigLength - maxHanging)
			{
				continue;
			}
			if(first.readEnd < second.readStart)
			{
				continue;
			}
			
			out.println(first.line + "\n" + second.line);
			tot++;
		}
	out.close();
	System.err.println("Number of useful alignments: " + tot);
}
/*
 * Takes in paf line assuming minimap2 call was:
 *     minimap2 <contigs> <reads>
 */
static class PafAlignment
{
	String readName, contigName;
	int readLength, readStart, readEnd;
	int contigLength, contigStart, contigEnd;
	char strand;
	String line;
	// Call with a seond parameter to denote that read and contig were flipped when calling minimap2
	PafAlignment(String line, int backwards)
	{
		this.line = line;
		String[] ss = line.split("\t");
		contigName = ss[0];
		contigLength = Integer.parseInt(ss[1]);
		contigStart = Integer.parseInt(ss[2]);
		contigEnd = Integer.parseInt(ss[3]);
		strand = ss[4].charAt(0);
		readName = ss[5];
		readLength = Integer.parseInt(ss[6]);
		readStart = Integer.parseInt(ss[7]);
		readEnd = Integer.parseInt(ss[8]);
	}
	PafAlignment(String line)
	{
		this.line = line;
		String[] ss = line.split("\t");
		readName = ss[0];
		readLength = Integer.parseInt(ss[1]);
		readStart = Integer.parseInt(ss[2]);
		readEnd = Integer.parseInt(ss[3]);
		strand = ss[4].charAt(0);
		contigName = ss[5];
		contigLength = Integer.parseInt(ss[6]);
		contigStart = Integer.parseInt(ss[7]);
		contigEnd = Integer.parseInt(ss[8]);
	}
}
}
